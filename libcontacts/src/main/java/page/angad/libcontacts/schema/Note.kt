package page.angad.libcontacts.schema

import android.provider.ContactsContract.CommonDataKinds.Note as DataNote

object Note : DataKind<Note>(DataNote.CONTENT_ITEM_TYPE) {
    val Note = field<String?>(DataNote.NOTE)
}
