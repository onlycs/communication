package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import android.provider.ContactsContract.CommonDataKinds.Relation as DataRelation

data class Relation(override val id: Long, val name: String?, val type: Type) :
    Field(DataRelation.CONTENT_ITEM_TYPE) {
    data class Type(override val type: Int, override val label: String) : TypeLabel() {
        companion object : TypeParser<Type>() {
            override val data: Class<*> = DataRelation::class.java

            override fun from(context: Context, type: Int?, label: String?): Type {
                val type = type ?: DataRelation.TYPE_FRIEND

                return Type(
                    type,
                    DataRelation.getTypeLabel(context.resources, type, label).toString()
                )
            }
        }
    }

    companion object : FieldParser<Relation>(DataRelation.CONTENT_ITEM_TYPE) {
        override fun parse(context: Context, c: Cursor): Relation? {
            val id = c.getLong(c.getColumnIndexOrThrow(DataRelation._ID))
            val name = c.getStringOrNull(c.getColumnIndexOrThrow(DataRelation.NAME))
            val type = Type.parse(context, c)

            return name?.let { Relation(id, it, type) }
        }
    }

    override fun write(context: Context, buffer: WriteBuffer) {
        name?.let { buffer.put(DataRelation.NAME, it) }
        buffer.put(context, type)
    }
}
