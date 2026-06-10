package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal as DataAddress

data class Address(
    override val id: Long,
    val display: String,
    val type: Type,
    val structured: Structured,
) : Field(DataAddress.CONTENT_ITEM_TYPE) {
    data class Structured(
        val street: String?, // 123 Main St
        val po: String?, // PO Box 123
        val neighborhood: String?, // Downtown
        val city: String?, // Springfield
        val region: String?, // IL
        val postcode: String?, // 62704
        val country: String?, // USA
    ) : FieldPartial() {
        companion object : PartialFieldParser<Structured>() {
            override fun parse(context: Context, c: Cursor): Structured {
                val street = c.getStringOrNull(c.getColumnIndexOrThrow(DataAddress.STREET))
                val po = c.getStringOrNull(c.getColumnIndexOrThrow(DataAddress.POBOX))
                val hood = c.getStringOrNull(c.getColumnIndexOrThrow(DataAddress.NEIGHBORHOOD))
                val city = c.getStringOrNull(c.getColumnIndexOrThrow(DataAddress.CITY))
                val region = c.getStringOrNull(c.getColumnIndexOrThrow(DataAddress.REGION))
                val postcode = c.getStringOrNull(c.getColumnIndexOrThrow(DataAddress.POSTCODE))
                val country = c.getStringOrNull(c.getColumnIndexOrThrow(DataAddress.COUNTRY))

                return Structured(street, po, hood, city, region, postcode, country)
            }
        }

        override fun write(context: Context, buffer: WriteBuffer) {
            street?.let { buffer.put(DataAddress.STREET, it) }
            po?.let { buffer.put(DataAddress.POBOX, it) }
            neighborhood?.let { buffer.put(DataAddress.NEIGHBORHOOD, it) }
            city?.let { buffer.put(DataAddress.CITY, it) }
            region?.let { buffer.put(DataAddress.REGION, it) }
            postcode?.let { buffer.put(DataAddress.POSTCODE, it) }
            country?.let { buffer.put(DataAddress.COUNTRY, it) }
        }

        override fun toString(): String {
            return listOfNotNull(street, po, neighborhood, city, region, postcode, country)
                .joinToString(", ")
        }
    }

    data class Type(override val type: Int, override val label: String) : TypeLabel() {
        companion object : TypeParser<Type>() {
            override val data: Class<*> = DataAddress::class.java

            override fun from(context: Context, type: Int?, label: String?): Type {
                val type = type ?: DataAddress.TYPE_OTHER
                return Type(
                    type,
                    DataAddress.getTypeLabel(context.resources, type, label).toString()
                )
            }
        }
    }

    companion object : FieldParser<Address>(DataAddress.CONTENT_ITEM_TYPE) {
        override fun parse(context: Context, c: Cursor): Address? {
            val id = c.getLong(c.getColumnIndexOrThrow(DataAddress._ID))
            val structured = Structured.parse(context, c)
            val address = c.getStringOrNull(c.getColumnIndexOrThrow(DataAddress.FORMATTED_ADDRESS))
            val type = Type.parse(context, c)

            val gen = structured.toString()
            if (gen.isBlank()) return null

            return Address(id, address ?: structured.toString(), type, structured)
        }
    }

    override fun write(
        context: Context,
        buffer: WriteBuffer,
    ) {
        type.label.let { buffer.put(DataAddress.LABEL, it) }
        buffer.put(context, type)
        buffer.put(context, structured)
    }
}