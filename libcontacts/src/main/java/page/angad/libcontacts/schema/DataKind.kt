package page.angad.libcontacts.schema

import android.provider.ContactsContract
import page.angad.libcontacts.Field
import page.angad.libcontacts.Kind
import page.angad.libcontacts.Table

/** Base of every [ContactsContract.Data] mimetype's field object. */
abstract class DataKind<K>(mimetype: String) : Kind<K>(Table.Data(mimetype)) {
    final override val Id: Field<K, Long> = field(ContactsContract.Data._ID)
    final override val ContactId: Field<K, Long> = field(ContactsContract.Data.CONTACT_ID)
    val RawContactId: Field<K, Long> = field(ContactsContract.Data.RAW_CONTACT_ID)
}

