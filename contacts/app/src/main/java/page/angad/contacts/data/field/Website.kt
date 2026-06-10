package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import android.provider.ContactsContract.CommonDataKinds.Website as DataWebsite

data class Website(override val id: Long, val url: String) : Field(DataWebsite.CONTENT_ITEM_TYPE) {
    companion object : FieldParser<Website>(DataWebsite.CONTENT_ITEM_TYPE) {
        override fun parse(context: Context, c: Cursor): Website? {
            val id = c.getLong(c.getColumnIndexOrThrow(DataWebsite._ID))
            val url = c.getStringOrNull(c.getColumnIndexOrThrow(DataWebsite.URL))

            return url?.let { Website(id, it) }
        }
    }

    override fun write(context: Context, buffer: WriteBuffer) {
        buffer.put(DataWebsite.URL, url)
    }
}