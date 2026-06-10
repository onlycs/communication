package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import android.provider.ContactsContract.CommonDataKinds.Email as DataEmail

data class Email(override val id: Long, val email: String, val type: Type) :
    Field(DataEmail.CONTENT_ITEM_TYPE) {
    data class Type(override val type: Int, override val label: String) : TypeLabel() {
        companion object : TypeParser<Type>() {
            override val data: Class<*> = DataEmail::class.java

            override fun from(context: Context, type: Int?, label: String?): Type {
                val type = type ?: DataEmail.TYPE_OTHER
                return Type(type, DataEmail.getTypeLabel(context.resources, type, label).toString())
            }
        }
    }

    companion object : FieldParser<Email>(DataEmail.CONTENT_ITEM_TYPE) {
        override fun parse(context: Context, c: Cursor): Email? {
            val id = c.getLong(c.getColumnIndexOrThrow(DataEmail._ID))
            val email = c.getStringOrNull(c.getColumnIndexOrThrow(DataEmail.ADDRESS))
            val type = Type.parse(context, c)

            return email?.let { Email(id, it, type) }
        }
    }

    override fun write(
        context: Context,
        buffer: WriteBuffer,
    ) {
        buffer.put(DataEmail.ADDRESS, email)
        buffer.put(context, type)
    }
}