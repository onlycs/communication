package page.angad.libcontacts.schema

import android.provider.ContactsContract
import page.angad.libcontacts.Field
import page.angad.libcontacts.Kind
import page.angad.libcontacts.Table

/** Aggregate contact fields ([ContactsContract.Contacts]). */
object Contacts : Kind<Contacts>(Table.Contacts) {
    override val Id: Field<Contacts, Long> = field(ContactsContract.Contacts._ID)
    override val ContactId: Field<Contacts, Long> get() = Id

    val DisplayName = field<String?>(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
    val PhotoUri = field<String?>(ContactsContract.Contacts.PHOTO_URI)
    val Starred = field<Boolean>(ContactsContract.Contacts.STARRED)
    val LookupKey = field<String?>(ContactsContract.Contacts.LOOKUP_KEY)
}
