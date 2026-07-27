package page.angad.libcontacts

import android.net.Uri
import android.provider.ContactsContract

/** A backing table of the contacts provider. */
sealed class Table(val contentUri: Uri) {
    /** Aggregate contacts ([ContactsContract.Contacts]). */
    data object Contacts : Table(ContactsContract.Contacts.CONTENT_URI)

    /** Per-account raw contacts ([ContactsContract.RawContacts]). */
    data object RawContacts : Table(ContactsContract.RawContacts.CONTENT_URI)

    /**
     * Typed data rows ([ContactsContract.Data]), scoped to a single [mimetype].
     * Queries against a [Data] table are automatically restricted to its mimetype.
     */
    data class Data(val mimetype: String) : Table(ContactsContract.Data.CONTENT_URI)
}
