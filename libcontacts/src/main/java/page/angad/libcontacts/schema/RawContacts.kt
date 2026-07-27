package page.angad.libcontacts.schema

import android.provider.ContactsContract
import page.angad.libcontacts.Field
import page.angad.libcontacts.Kind
import page.angad.libcontacts.Table

/** Per-account raw contact fields ([ContactsContract.RawContacts]). */
object RawContacts : Kind<RawContacts>(Table.RawContacts) {
    override val Id: Field<RawContacts, Long> = field(ContactsContract.RawContacts._ID)
    override val ContactId: Field<RawContacts, Long> = field(ContactsContract.RawContacts.CONTACT_ID)

    val AccountType = field<String?>(ContactsContract.RawContacts.ACCOUNT_TYPE)
    val AccountName = field<String?>(ContactsContract.RawContacts.ACCOUNT_NAME)
}
