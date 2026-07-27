package page.angad.libcontacts.schema

import android.provider.ContactsContract.CommonDataKinds.Photo as DataPhoto

object Photo : DataKind<Photo>(DataPhoto.CONTENT_ITEM_TYPE) {
    /** The contact's photo uri, joined from the Contacts table; read-only. */
    val PhotoUri = field<String?>(DataPhoto.PHOTO_URI)
}
