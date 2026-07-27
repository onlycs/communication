package page.angad.libcontacts

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import page.angad.libcontacts.schema.Contacts

/**
 * A shareable vCard export of one contact, served by the provider itself.
 */
data class VCard(val uri: Uri, val mimeType: String, val suggestedFileName: String)

/** One [VCard] per id found; ids with no matching contact are silently skipped. */
internal suspend fun vCardQuery(resolver: ContentResolver, ids: List<Long>): List<VCard> =
    withContext(Dispatchers.IO) {
        queryRows(
            resolver,
            Contacts,
            listOf(Contacts.LookupKey, Contacts.DisplayName),
            listOf(Contacts.Id inList ids),
            null
        ).mapNotNull { row ->
            val lookupKey = row[Contacts.LookupKey] ?: return@mapNotNull null

            val uri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_VCARD_URI,
                Uri.encode(lookupKey)
            )

            VCard(
                uri,
                ContactsContract.Contacts.CONTENT_VCARD_TYPE,
                "${
                    row[Contacts.DisplayName]
                        ?.replace(Regex("[^a-zA-Z0-9 ._-]"), "")
                        ?.takeIf { it.isNotEmpty() }
                        ?: "Contact"
                }.vcf"
            )
        }
    }