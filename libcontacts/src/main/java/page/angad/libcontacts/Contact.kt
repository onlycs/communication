package page.angad.libcontacts

import android.content.ContentResolver
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
    operator fun <K : Kind<K>> get(kind: K): List<Row<K>> = rows[kind].orEmpty() as List<Row<K>>

    /**
     * Re-reads all selected fields, returning a new [Contact], or `null` if the contact
     * no longer exists.
     */
    suspend fun reload(): Contact? = withContext(Dispatchers.IO) {
        findContacts(resolver, requested, listOf(Contacts.Id eq id), emptyList()).firstOrNull()
    }

    /** vCard export of this contact, ready for a share intent. */
    suspend fun vcard(): VCard = vCardQuery(resolver, listOf(id)).first()

    override fun equals(other: Any?) =
        other is Contact && other.rows == rows

    override fun hashCode() = rows.hashCode()

    override fun toString() = "Contact($contact, subRows=$rows)"
}
