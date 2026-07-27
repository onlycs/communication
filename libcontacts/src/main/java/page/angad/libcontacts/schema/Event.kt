package page.angad.libcontacts.schema

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.BaseTypes
import page.angad.libcontacts.Row
import page.angad.libcontacts.schema.Event.Label
import page.angad.libcontacts.schema.Event.Type
import android.provider.ContactsContract.CommonDataKinds.Event as DataEvent

object Event : DataKind<Event>(DataEvent.CONTENT_ITEM_TYPE) {
    val StartDate = field<String?>(DataEvent.START_DATE)
    val Type = field<Int?>(DataEvent.TYPE)
    val Label = field<String?>(DataEvent.LABEL)

    /** Human-readable label for [row]'s [Type]/[Label], e.g. "Birthday". */
    fun typeLabel(context: Context, row: Row<Event>): String {
        val type = row[Type]
        val label = row[Label]

        // Event has no getTypeLabel(); mirror its custom-label convention over getTypeResource().
        return if ((type == null || type == BaseTypes.TYPE_CUSTOM) && !label.isNullOrEmpty()) label
        else context.resources.getString(DataEvent.getTypeResource(type))
    }
}
