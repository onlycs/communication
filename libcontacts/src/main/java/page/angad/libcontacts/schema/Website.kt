package page.angad.libcontacts.schema

import android.provider.ContactsContract.CommonDataKinds.Website as DataWebsite

object Website : DataKind<Website>(DataWebsite.CONTENT_ITEM_TYPE) {
    val Url = field<String?>(DataWebsite.URL)
}
