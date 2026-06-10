package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import android.provider.ContactsContract.CommonDataKinds.StructuredName as DataName

data class Name(
    override val id: Long,
    val display: String,
    val structured: Structured,
    val phonetic: Phonetic,
) : Field(DataName.CONTENT_ITEM_TYPE) {
    data class Structured(
        val prefix: String?,
        val given: String?,
        val middle: String?,
        val family: String?,
        val suffix: String?,
    ) : FieldPartial() {
        companion object : PartialFieldParser<Structured>() {
            override fun parse(context: Context, c: Cursor): Structured {
                val prefix = c.getStringOrNull(c.getColumnIndexOrThrow(DataName.PREFIX))
                val given = c.getStringOrNull(c.getColumnIndexOrThrow(DataName.GIVEN_NAME))
                val middle = c.getStringOrNull(c.getColumnIndexOrThrow(DataName.MIDDLE_NAME))
                val family = c.getStringOrNull(c.getColumnIndexOrThrow(DataName.FAMILY_NAME))
                val suffix = c.getStringOrNull(c.getColumnIndexOrThrow(DataName.SUFFIX))

                return Structured(prefix, given, middle, family, suffix)
            }
        }

        override fun write(context: Context, buffer: WriteBuffer) {
            prefix?.let { buffer.put(DataName.PREFIX, it) }
            given?.let { buffer.put(DataName.GIVEN_NAME, it) }
            middle?.let { buffer.put(DataName.MIDDLE_NAME, it) }
            family?.let { buffer.put(DataName.FAMILY_NAME, it) }
            suffix?.let { buffer.put(DataName.SUFFIX, it) }
        }

        override fun toString(): String {
            val s = listOfNotNull(prefix, given, middle, family, suffix).joinToString(" ")
            return s.ifBlank { "(No name)" }
        }
    }

    data class Phonetic(
        val given: String?,
        val middle: String?,
        val family: String?,
    ) : FieldPartial() {
        companion object : PartialFieldParser<Phonetic>() {
            override fun parse(context: Context, c: Cursor): Phonetic {
                return Phonetic(
                    c.getStringOrNull(c.getColumnIndexOrThrow(DataName.PHONETIC_GIVEN_NAME)),
                    c.getStringOrNull(c.getColumnIndexOrThrow(DataName.PHONETIC_MIDDLE_NAME)),
                    c.getStringOrNull(c.getColumnIndexOrThrow(DataName.PHONETIC_FAMILY_NAME))
                )
            }
        }

        override fun write(context: Context, buffer: WriteBuffer) {
            given?.let { buffer.put(DataName.PHONETIC_GIVEN_NAME, it) }
            middle?.let { buffer.put(DataName.PHONETIC_MIDDLE_NAME, it) }
            family?.let { buffer.put(DataName.PHONETIC_FAMILY_NAME, it) }
        }
    }

    companion object : FieldParser<Name>(DataName.CONTENT_ITEM_TYPE) {
        override fun parse(context: Context, c: Cursor): Name {
            val id = c.getLong(c.getColumnIndexOrThrow(DataName._ID))
            val display = c.getStringOrNull(c.getColumnIndexOrThrow(DataName.DISPLAY_NAME))
            val structured = Structured.parse(context, c)
            val phonetic = Phonetic.parse(context, c)

            return Name(id, display ?: structured.toString(), structured, phonetic)
        }
    }

    override fun write(context: Context, buffer: WriteBuffer) {
        buffer.put(DataName.DISPLAY_NAME, display)
        buffer.put(context, structured)
        buffer.put(context, phonetic)
    }
}
