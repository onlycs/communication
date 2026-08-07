package page.angad.libcontacts

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import page.angad.libcontacts.schema.Contacts

/**
 * One aggregate contact from a select: contact-level values are read directly
 * (`contact[Contacts.DisplayName]`), other kinds as sub-row lists (`contact[Phone]`).
 */
class Contact internal constructor(
    private val resolver: ContentResolver,
    private val requested: List<Field<*, *>>,
    private val rows: Map<Kind<*>, List<Row<*>>>,
) {
    @Suppress("UNCHECKED_CAST")
    private val contact = rows[Contacts]!![0] as Row<Contacts>

    val id: Long get() = contact.id

    operator fun <T> get(field: Field<Contacts, T>): T = contact[field]

    /** This contact's rows of [kind], e.g. `contact[Phone]`. */
    @Suppress("UNCHECKED_CAST")
    operator fun <K> get(kind: Kind<K>): List<Row<K>> = rows[kind].orEmpty() as List<Row<K>>

    /**
     * Re-reads all selected fields, returning a new [Contact], or `null` if the contact
     * no longer exists.
     */
    suspend fun reload(): Contact? = withContext(Dispatchers.IO) {
        findContacts(resolver, requested, listOf(Contacts.Id eq id), emptyList()).firstOrNull()
    }

    /**
     * This contact's lookup uri — the id-plus-lookup-key uri that survives the contact
     * being re-aggregated, and what `ACTION_VIEW`/`ACTION_EDIT` and shortcuts expect.
     * `null` if the contact no longer exists or has no lookup key.
     *
     * Selecting [Contacts.LookupKey] avoids the extra query this otherwise runs.
     */
    suspend fun lookupUri(): Uri? = withContext(Dispatchers.IO) {
        val lookupKey =
            if (Contacts.LookupKey in contact) contact[Contacts.LookupKey]
            else queryRows(
                resolver,
                Contacts,
                listOf(Contacts.LookupKey),
                listOf(Contacts.Id eq id),
                null
            ).firstOrNull()?.get(Contacts.LookupKey)

        lookupKey?.let { ContactsContract.Contacts.getLookupUri(id, it) }
    }

    /** vCard export of this contact, ready for a share intent. */
    suspend fun vcard(): VCard = vCardsQuery(resolver, listOf(id)).first()

    override fun equals(other: Any?) =
        other is Contact && other.rows == rows

    override fun hashCode() = rows.hashCode()

    override fun toString() = "Contact($contact, subRows=$rows)"
}
