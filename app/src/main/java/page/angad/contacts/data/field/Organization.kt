package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getStringOrNull
import android.provider.ContactsContract.CommonDataKinds.Organization as DataOrg

data class Organization(
    override val id: Long,
    val company: String?,
    val title: String?,
    val dept: String?,
) : Field(DataOrg.CONTENT_ITEM_TYPE) {
    companion object : FieldParser<Organization>(DataOrg.CONTENT_ITEM_TYPE) {
        override fun parse(context: Context, c: Cursor): Organization? {
            val id = c.getLong(c.getColumnIndexOrThrow(DataOrg._ID))
            val company = c.getStringOrNull(c.getColumnIndexOrThrow(DataOrg.COMPANY))
            val title = c.getStringOrNull(c.getColumnIndexOrThrow(DataOrg.TITLE))
            val dept = c.getStringOrNull(c.getColumnIndexOrThrow(DataOrg.DEPARTMENT))

            return if (company != null || title != null || dept != null) {
                Organization(id, company, title, dept)
            } else {
                null
            }
        }
    }

    override fun write(context: Context, buffer: WriteBuffer) {
        company?.let { buffer.put(DataOrg.COMPANY, it) }
        title?.let { buffer.put(DataOrg.TITLE, it) }
        dept?.let { buffer.put(DataOrg.DEPARTMENT, it) }
    }
}