package page.angad.libcontacts.schema

import android.content.Context
import page.angad.libcontacts.Row
import page.angad.libcontacts.schema.Phone.Label
import page.angad.libcontacts.schema.Phone.Type
import android.provider.ContactsContract.CommonDataKinds.Phone as DataPhone

object Phone : DataKind<Phone>(DataPhone.CONTENT_ITEM_TYPE) {
    val Number = field<String?>(DataPhone.NUMBER)
    val Type = field<Int?>(DataPhone.TYPE)
    val Label = field<String?>(DataPhone.LABEL)

    /** Human-readable label for [row]'s [Type]/[Label], e.g. "Mobile". */
    fun typeLabel(context: Context, row: Row<Phone>): String = DataPhone.getTypeLabel(
        context.resources,
        row[Type] ?: DataPhone.TYPE_OTHER,
        row[Label]
    ).toString()
}
