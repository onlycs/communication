package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import android.provider.ContactsContract.CommonDataKinds.Phone as DataPhone

data class Phone(override val id: Long, val number: String, val type: Type) :
    Field(DataPhone.CONTENT_ITEM_TYPE) {
    data class Type(override val type: Int, override val label: String) : TypeLabel() {
        companion object : TypeParser<Type>() {
            override val data: Class<*> = DataPhone::class.java

            override fun from(context: Context, type: Int?, label: String?): Type {
                val type = type ?: DataPhone.TYPE_OTHER
                return Type(type, DataPhone.getTypeLabel(context.resources, type, label).toString())
            }
        }
    }

    companion object : FieldParser<Phone>(DataPhone.CONTENT_ITEM_TYPE) {
        override fun parse(context: Context, c: Cursor): Phone? {
            val id = c.getLong(c.getColumnIndexOrThrow(DataPhone._ID))
            val number = c.getStringOrNull(c.getColumnIndexOrThrow(DataPhone.NUMBER))
            val type = Type.parse(context, c)

            return number?.let { Phone(id, number, type) }
        }
    }

    override fun write(context: Context, buffer: WriteBuffer) {
        buffer.put(DataPhone.NUMBER, number)
        buffer.put(context, type)
    }
}