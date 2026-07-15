package page.angad.contacts.ui.list

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import contacts.core.entities.Contact
import page.angad.contacts.ui.common.ContactPhoto
import page.angad.contacts.util.appName
import page.angad.uicore.GroupedListItemData

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactListItem(
    data: GroupedListItemData<Contact, Char>,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        onClick = {},
        shapes = data.shape(),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ContactPhoto(contact = data.value)

            Text(
                data.value.displayNamePrimary ?: "(No name)",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(Modifier.weight(1F))

            AccountLabel(
                contact = data.value,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun AccountLabel(contact: Contact, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val account = contact.rawContacts.find { it.account != null }?.account

    Text(
        text = account?.let { appName(context, it.type) } ?: "Device",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}