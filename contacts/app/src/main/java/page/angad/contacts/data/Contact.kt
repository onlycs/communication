package page.angad.contacts.data

import android.content.Context
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.RawContacts
import androidx.core.database.getStringOrNull
import page.angad.contacts.data.field.Address
import page.angad.contacts.data.field.Email
import page.angad.contacts.data.field.Event
import page.angad.contacts.data.field.Name
import page.angad.contacts.data.field.Note
import page.angad.contacts.data.field.Organization
import page.angad.contacts.data.field.Phone
import page.angad.contacts.data.field.Photo
import page.angad.contacts.data.field.Relation
import page.angad.contacts.data.field.Repository
import page.angad.contacts.data.field.Website
import page.angad.contacts.data.field.many
import page.angad.contacts.data.field.one

data class Contact(
    private val context: Context,
    val id: Long,
    val repo: Repository,
) {
    val addresses = many(Address, context, this)
    val emails = many(Email, context, this)
    val events = many(Event, context, this)
    val name = one(Name, context, this)
    val note = one(Note, context, this)
    val org = one(Organization, context, this)
    val phones = many(Phone, context, this)
    val photo = one(Photo, context, this)
    val relations = many(Relation, context, this)
    val websites = many(Website, context, this)

    companion object {
        fun query(context: Context): List<Contact> {
            val contacts = mutableListOf<Contact>()
            val cursor = context.contentResolver.query(
                Contacts.CONTENT_URI,
                arrayOf(
                    Contacts._ID,
                    RawContacts.ACCOUNT_NAME,
                    RawContacts.ACCOUNT_TYPE
                ),
                null,
                null,
                null
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(Contacts._ID))
                    val act = it.getStringOrNull(it.getColumnIndexOrThrow(RawContacts.ACCOUNT_TYPE))
                    val acn = it.getStringOrNull(it.getColumnIndexOrThrow(RawContacts.ACCOUNT_NAME))
                    val repo = Repository.from(context, act, acn)
                    contacts.add(Contact(context, id, repo))
                }
            }

            return contacts
        }
    }
}