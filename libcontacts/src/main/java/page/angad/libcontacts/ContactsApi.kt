package page.angad.libcontacts

import android.accounts.Account
import android.content.Context

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

    fun update(body: (AssignmentScope) -> Unit) =
        UpdateQuery(resolver, AssignmentScope().also(body).assignments)

    /**
     * Deletes rows of [kind]. To delete whole contacts, target `RawContacts` by contact
     * id — the provider does not support bulk deletes on the aggregate contacts uri.
     */
    fun delete(kind: Kind<*>) = DeleteQuery(resolver, kind)

    /** Inserts one data row; attach it to a raw contact via `where(RawContactId eq id)`. */
    fun insert(body: (AssignmentScope) -> Unit) =
        InsertQuery(resolver, AssignmentScope().also(body).assignments)

    /** Starts a new contact; a `null` [account] creates a local, on-device contact. */
    fun new(account: Account? = null) = NewContactBuilder(resolver, account)

    /** One vCard per id found; ids with no matching contact are silently skipped. */
    suspend fun vCards(ids: List<Long>): List<VCard> = vCardQuery(resolver, ids)
}
