package page.angad.libcontacts.schema

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.BaseTypes
import page.angad.libcontacts.Row
import page.angad.libcontacts.schema.Relation.Label
import page.angad.libcontacts.schema.Relation.Type
import android.provider.ContactsContract.CommonDataKinds.Relation as DataRelation

object Relation : DataKind<Relation>(DataRelation.CONTENT_ITEM_TYPE) {
    val Name = field<String?>(DataRelation.NAME)
    val Type = field<Int?>(DataRelation.TYPE)
    val Label = field<String?>(DataRelation.LABEL)

    /** Human-readable label for [row]'s [Type]/[Label], e.g. "Sister". */
    fun typeLabel(context: Context, row: Row<Relation>): String = DataRelation.getTypeLabel(
        context.resources,
        row[Type] ?: BaseTypes.TYPE_CUSTOM,
        row[Label]
    ).toString()
}
