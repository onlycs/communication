package page.angad.libcontacts.schema

import android.content.Context
import page.angad.libcontacts.Row
import page.angad.libcontacts.schema.Email.Label
import page.angad.libcontacts.schema.Email.Type
import android.provider.ContactsContract.CommonDataKinds.Email as DataEmail

object Email : DataKind<Email>(DataEmail.CONTENT_ITEM_TYPE) {
    val Address = field<String?>(DataEmail.ADDRESS)
    val Type = field<Int?>(DataEmail.TYPE)
    val Label = field<String?>(DataEmail.LABEL)

    /** Human-readable label for [row]'s [Type]/[Label], e.g. "Work". */
    fun typeLabel(context: Context, row: Row<Email>): String = DataEmail.getTypeLabel(
        context.resources,
        row[Type] ?: DataEmail.TYPE_OTHER,
        row[Label]
    ).toString()
}
