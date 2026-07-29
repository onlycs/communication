package page.angad.libcontacts

import android.accounts.Account
import android.content.Context
import page.angad.libcontacts.schema.DataKind
import page.angad.libcontacts.schema.RawContacts

/**
 * Entry point to the contacts provider.
 *
 * ```
 * val api = ContactsApi(context)
 *
 * api.select(Contacts.Id, Contacts.DisplayName)
 *     .orderBy(Contacts.DisplayName.asc(ignoreCase = true))
 *     .find()
 *
 * api.update { it[Contacts.Starred] = true }
 *     .where(Contacts.Id inList ids)
 *     .commit()
 * ```
 *
 * Requires `READ_CONTACTS`/`WRITE_CONTACTS` to be granted before use.
 */
class ContactsApi(context: Context) {
    private val resolver = context.contentResolver

    fun select(vararg fields: Field<*, *>) = SelectQuery(resolver, fields.toList())

    /** Updates rows of one kind; the fields assigned in [body] must all share it. */
    fun <K> update(body: (AssignmentScope<K>) -> Unit) =
        UpdateQuery(resolver, AssignmentScope<K>().also(body).assignments)

    /** Deletes data rows of [kind]. */
    fun <K> delete(kind: DataKind<K>) = DeleteQuery(resolver, kind)

    /** Deletes raw contacts; delete whole contacts via `RawContacts.ContactId`. */
    fun delete(kind: RawContacts) = DeleteQuery(resolver, kind)

    /** Inserts one data row; `commit(rawContactId)` attaches it to that raw contact. */
    fun <K : DataKind<*>> insert(body: (AssignmentScope<K>) -> Unit) =
        InsertQuery(resolver, AssignmentScope<K>().also(body).assignments)

    /** Starts a new contact; a `null` [account] creates a local, on-device contact. */
    fun new(account: Account? = null) = NewContactBuilder(resolver, account)

    /** One vCard per id found; ids with no matching contact are silently skipped. */
    suspend fun vCards(ids: List<Long>): List<VCard> = vCardsQuery(resolver, ids)

    /** All contacts found for [ids] in a single `contacts.vcf`; `null` if none exist. */
    suspend fun vCardCombined(ids: List<Long>): VCard? = vCardCombinedQuery(resolver, ids)
}
