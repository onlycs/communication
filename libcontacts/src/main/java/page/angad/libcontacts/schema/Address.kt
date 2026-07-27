package page.angad.libcontacts.schema

import android.content.Context
import page.angad.libcontacts.Row
import page.angad.libcontacts.schema.Address.Label
import page.angad.libcontacts.schema.Address.Type
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal as DataAddress

object Address : DataKind<Address>(DataAddress.CONTENT_ITEM_TYPE) {
    val FormattedAddress = field<String?>(DataAddress.FORMATTED_ADDRESS)
    val Street = field<String?>(DataAddress.STREET)
    val PoBox = field<String?>(DataAddress.POBOX)
    val Neighborhood = field<String?>(DataAddress.NEIGHBORHOOD)
    val City = field<String?>(DataAddress.CITY)
    val Region = field<String?>(DataAddress.REGION)
    val Postcode = field<String?>(DataAddress.POSTCODE)
    val Country = field<String?>(DataAddress.COUNTRY)
    val Type = field<Int?>(DataAddress.TYPE)
    val Label = field<String?>(DataAddress.LABEL)

    /** Human-readable label for [row]'s [Type]/[Label], e.g. "Home". */
    fun typeLabel(context: Context, row: Row<Address>): String = DataAddress.getTypeLabel(
        context.resources,
        row[Type] ?: DataAddress.TYPE_OTHER,
        row[Label]
    ).toString()
}
