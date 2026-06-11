package page.angad.contacts.data.field

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import android.provider.ContactsContract.CommonDataKinds.Photo as DataPhoto

data class Photo(override val id: Long, val uri: Uri) : Field(DataPhoto.CONTENT_ITEM_TYPE) {
    companion object : FieldParser<Photo>(DataPhoto.CONTENT_ITEM_TYPE) {
        override fun parse(context: Context, c: android.database.Cursor): Photo? {
            val id = c.getLong(c.getColumnIndexOrThrow(DataPhoto._ID))
            val uriStr = c.getString(c.getColumnIndexOrThrow(DataPhoto.PHOTO_URI))
            val uri = uriStr?.toUri()

            return uri?.let { Photo(id, it) }
        }
    }

    override fun write(context: Context, buffer: WriteBuffer) {
        buffer.put(DataPhoto.PHOTO_URI, uri.toString())
    }
}
