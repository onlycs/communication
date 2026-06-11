package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract.Data

abstract class BaseFieldParser<T : BaseField> {
    abstract fun parse(context: Context, c: Cursor): T?
}

abstract class PartialFieldParser<T : FieldPartial> : BaseFieldParser<T>() {
    fun parse(context: Context, id: Long): T? {
        val c = context.contentResolver.query(
            Data.CONTENT_URI,
            null,
            "${Data._ID} = ?",
            arrayOf(id.toString()),
            null
        )

        c?.use {
            if (it.moveToFirst()) {
                return parse(context, it)
            }

            it.close()
        }

        return null
    }
}

abstract class FieldParser<T : Field>(val mime: String) : BaseFieldParser<T>() {
    fun query(context: Context, contactId: Long): MutableList<T> {
        val fields = mutableListOf<T>()
        val c = context.contentResolver.query(
            Data.CONTENT_URI,
            null,
            "${Data.CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?",
            arrayOf(contactId.toString(), mime),
            null
        )

        c?.use {
            while (it.moveToNext()) {
                parse(context, it)?.let { field -> fields += field }
            }
        }

        return fields
    }

    fun queryOne(context: Context, contactId: Long): T? {
        val c = context.contentResolver.query(
            Data.CONTENT_URI,
            null,
            "${Data.CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?",
            arrayOf(contactId.toString(), mime),
            null
        )

        c?.use {
            if (it.moveToFirst()) {
                return parse(context, it)
            }

            it.close()
        }

        return null
    }

    fun queryBy(context: Context, id: Long): T? {
        val c = context.contentResolver.query(
            Data.CONTENT_URI,
            null,
            "${Data._ID} = ? AND ${Data.MIMETYPE} = ?",
            arrayOf(id.toString(), mime),
            null
        )

        c?.use {
            if (it.moveToFirst()) {
                return parse(context, it)
            }

            it.close()
        }

        return null
    }
}
