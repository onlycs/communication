package page.angad.libcontacts

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import page.angad.libcontacts.schema.Contacts
import page.angad.libcontacts.schema.DataKind

/** Ceiling on ids inlined into an `IN (...)` clause (SQLite's host-variable limit is 999). */
private const val MAX_IN_IDS = 500

internal fun <K : Kind<K>> resolveKind(fields: List<Field<K, *>>): K {
    require(fields.isNotEmpty()) { "at least one assignment is required" }
    return fields.first().kind
}

/** Narrows fields to this kind after validating they belong to it. */
@Suppress("UNCHECKED_CAST")
private fun <K : Kind<K>> Kind<K>.own(fields: List<Field<*, *>>): List<Field<K, *>> {
    require(fields.all { it.kind == this }) { "fields do not belong to $this: $fields" }
    return fields as List<Field<K, *>>
}

internal fun selectionOf(table: Table, filters: List<Filter<*>>): Pair<String?, Array<String>?> {
    val parts = filters.map { it.render() } + listOfNotNull(
        (table as? Table.Data)?.let { "${ContactsContract.Data.MIMETYPE} = ?" to listOf(it.mimetype) },
    )

    if (parts.isEmpty()) return null to null

    return parts.joinToString(" AND ") { "(${it.first})" } to
            parts.flatMap { it.second }.toTypedArray()
}

private fun List<Assignment<*, *>>.toContentValues() = ContentValues().apply {
    forEach { it.field.type.put(this, it.field.column, it.value) }
}

/** Runs one provider query against a single kind. */
internal fun <K : Kind<K>> queryRows(
    resolver: ContentResolver,
    kind: Kind<K>,
    fields: List<Field<K, *>>,
    filters: List<Filter<K>>,
    order: String?,
): List<Row<K>> {
    val selected = (fields + kind.Id + kind.ContactId).distinct()
    val (selection, args) = selectionOf(kind.table, filters)
    val rows = mutableListOf<Row<K>>()

    resolver.query(
        kind.table.contentUri,
        selected.map { it.column }.toTypedArray(),
        selection,
        args,
        order
    )?.use { c ->
        val indices = selected.associateWith { c.getColumnIndexOrThrow(it.column) }
        while (c.moveToNext()) {
            rows += Row(kind, indices.entries.associate { (f, i) -> f to f.type.read(c, i) })
        }
    }

    return rows
}

/** Ids of the contacts having at least one row matching [filter]. */
private fun <K : Kind<K>> contactIdsMatching(
    resolver: ContentResolver,
    filter: Filter<K>
): Set<Long> =
    queryRows(resolver, filter.kind(), emptyList(), listOf(filter), null)
        .map { it.contactId }
        .toSet()

/** This kind's rows for [contactIds], grouped by contact. */
private fun <K : Kind<K>> subRowsByContact(
    resolver: ContentResolver,
    kind: Kind<K>,
    fields: List<Field<*, *>>,
    contactIds: Set<Long>,
): Map<Long, List<Row<K>>> {
    // Above MAX_IN_IDS, fetch unconstrained and drop foreign rows in memory instead.
    val filters =
        if (contactIds.size <= MAX_IN_IDS) listOf(kind.ContactId inList contactIds)
        else emptyList()

    return queryRows(resolver, kind, kind.own(fields), filters, null)
        .filter { it.contactId in contactIds }
        .groupBy { it.contactId }
}

internal fun findContacts(
    resolver: ContentResolver,
    requested: List<Field<*, *>>,
    filters: List<Filter<*>>,
    order: List<OrderSpec<Contacts>>,
): List<Contact> {
    val fieldsByKind = requested.groupBy { it.kind }

    // Contact-level filters apply to the main query directly; a filter on any other
    // kind means "the contact has a matching row" and becomes an id constraint.
    val (direct, byRow) = filters.partition { it.kind() == Contacts }

    @Suppress("UNCHECKED_CAST")
    val contactFilters = (direct as List<Filter<Contacts>>).toMutableList()

    byRow
        .map { contactIdsMatching(resolver, it) }
        .reduceOrNull(Set<Long>::intersect)
        ?.let { contactFilters += Contacts.Id inList it }

    val contactRows = queryRows(
        resolver,
        Contacts,
        Contacts.own(fieldsByKind[Contacts].orEmpty()),
        contactFilters,
        order.takeIf { it.isNotEmpty() }?.render()
    )

    val contactIds = contactRows.map { it.id }.toSet()
    val subRows = fieldsByKind
        .filterKeys { it != Contacts }
        .mapValues { (kind, fields) -> subRowsByContact(resolver, kind, fields, contactIds) }

    return contactRows.map { row ->
        val rows = buildMap {
            put(Contacts, listOf(row))
            subRows.forEach { (kind, grouped) -> put(kind, grouped[row.id].orEmpty()) }
        }

        Contact(resolver, requested, rows)
    }
}

class SelectQuery internal constructor(
    private val resolver: ContentResolver,
    private val fields: List<Field<*, *>>,
) {
    private val filters = mutableListOf<Filter<*>>()
    private var order = emptyList<OrderSpec<Contacts>>()

    /**
     * Restricts results; repeated calls are combined with AND. A filter on a non-contact
     * kind selects contacts having a matching row.
     */
    fun where(filter: Filter<*>) = apply { filters += filter }

    fun orderBy(vararg specs: OrderSpec<Contacts>) = apply { order = order + specs }

    suspend fun find(): List<Contact> = withContext(Dispatchers.IO) {
        findContacts(resolver, fields, filters, order)
    }
}

class UpdateQuery<K : Kind<K>> internal constructor(
    private val resolver: ContentResolver,
    private val assignments: List<Assignment<K, *>>,
) {
    /** Restricts the affected rows; [commit] only exists after at least one filter. */
    fun where(filter: Filter<K>) = FilteredUpdate(resolver, assignments, listOf(filter))
}

class FilteredUpdate<K : Kind<K>> internal constructor(
    private val resolver: ContentResolver,
    private val assignments: List<Assignment<K, *>>,
    private val filters: List<Filter<K>>,
) {
    /** Further restricts the affected rows; repeated calls are combined with AND. */
    fun where(filter: Filter<K>) = FilteredUpdate(resolver, assignments, filters + filter)

    /** Returns the number of updated rows. */
    suspend fun commit(): Int = withContext(Dispatchers.IO) {
        val kind = resolveKind(assignments.map { it.field })
        val (selection, args) = selectionOf(kind.table, filters)
        resolver.update(kind.table.contentUri, assignments.toContentValues(), selection, args)
    }
}

class DeleteQuery<K : Kind<K>> internal constructor(
    private val resolver: ContentResolver,
    private val kind: Kind<K>,
) {
    /** Restricts the affected rows; [commit] only exists after at least one filter. */
    fun where(filter: Filter<K>) = FilteredDelete(resolver, kind, listOf(filter))
}

class FilteredDelete<K : Kind<K>> internal constructor(
    private val resolver: ContentResolver,
    private val kind: Kind<K>,
    private val filters: List<Filter<K>>,
) {
    /** Further restricts the affected rows; repeated calls are combined with AND. */
    fun where(filter: Filter<K>) = FilteredDelete(resolver, kind, filters + filter)

    /** Returns the number of deleted rows. */
    suspend fun commit(): Int = withContext(Dispatchers.IO) {
        val (selection, args) = selectionOf(kind.table, filters)
        resolver.delete(kind.table.contentUri, selection, args)
    }
}

class InsertQuery<K : DataKind<K>> internal constructor(
    private val resolver: ContentResolver,
    private val assignments: List<Assignment<K, *>>,
) {
    /** Inserts the row onto the raw contact [rawContactId]; returns the new data row's id. */
    suspend fun commit(rawContactId: Long): Long = withContext(Dispatchers.IO) {
        val kind = resolveKind(assignments.map { it.field })
        val values = assignments.toContentValues()
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
        values.put(ContactsContract.Data.MIMETYPE, kind.mimetype)

        val uri = checkNotNull(
            resolver.insert(
                ContactsContract.Data.CONTENT_URI,
                values
            )
        ) { "insert failed" }
        ContentUris.parseId(uri)
    }
}

class NewContactBuilder internal constructor(
    private val resolver: ContentResolver,
    private val account: Account?,
) {
    private val rows = mutableListOf<Pair<String, List<Assignment<*, *>>>>()

    /** Adds one data row (name, phone, email, ...) to the new contact. */
    fun <K : DataKind<K>> add(body: (AssignmentScope<K>) -> Unit) = apply {
        val assignments = AssignmentScope<K>().also(body).assignments
        rows += resolveKind(assignments.map { it.field }).mimetype to assignments
    }

    /** Atomically creates the raw contact and its data rows; returns the new raw contact's id. */
    suspend fun commit(): Long = withContext(Dispatchers.IO) {
        val ops = arrayListOf(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account?.type)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account?.name)
                .build()
        )

        rows.forEach { (mimetype, assignments) ->
            val values = assignments.toContentValues()
            values.put(ContactsContract.Data.MIMETYPE, mimetype)

            // The raw contact's id doesn't exist until the batch runs, so each data
            // row back-references the result of the RawContacts insert at index 0.
            ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValues(values)
                .build()
        }

        val results = resolver.applyBatch(ContactsContract.AUTHORITY, ops)
        ContentUris.parseId(checkNotNull(results[0].uri) { "raw contact insert failed" })
    }
}