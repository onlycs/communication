package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import android.provider.ContactsContract.CommonDataKinds.Event as DataEvent

data class Event(override val id: Long, val date: String, val type: Type) :
    Field(DataEvent.CONTENT_ITEM_TYPE) {
    data class Type(override val type: Int, override val label: String) : TypeLabel() {
        companion object : TypeParser<Type>() {
            override val data: Class<*> = DataEvent::class.java

            override fun from(context: Context, type: Int?, label: String?): Type {
                val type = type ?: DataEvent.TYPE_OTHER
                return Type(type, DataEvent.getTypeLabel(context.resources, type, label).toString())
            }
        }
    }

    companion object : FieldParser<Event>(DataEvent.CONTENT_ITEM_TYPE) {
        override fun parse(
            context: Context,
            c: Cursor,
        ): Event? {
            val id = c.getLong(c.getColumnIndexOrThrow(DataEvent._ID))
            val date = c.getStringOrNull(c.getColumnIndexOrThrow(DataEvent.START_DATE))
            val type = Type.parse(context, c)

            return date?.let { Event(id, it, type) }
        }
    }

    override fun write(context: Context, buffer: WriteBuffer) {
        buffer.put(DataEvent.START_DATE, date)
        buffer.put(context, type)
    }
}