package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import android.provider.ContactsContract.CommonDataKinds.Note as DataNote

data class Note(override val id: Long, val note: String) : Field(DataNote.CONTENT_ITEM_TYPE) {
    companion object : FieldParser<Note>(DataNote.CONTENT_ITEM_TYPE) {
        override fun parse(context: Context, c: Cursor): Note? {
            val id = c.getLong(c.getColumnIndexOrThrow(DataNote._ID))
            val note = c.getStringOrNull(c.getColumnIndexOrThrow(DataNote.NOTE))

            return note?.let { Note(id, it) }
        }
    }

    override fun write(context: Context, buffer: WriteBuffer) {
        buffer.put(DataNote.NOTE, note)
    }
}
