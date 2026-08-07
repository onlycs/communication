package page.angad.contacts.ui.main.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import page.angad.contacts.ui.common.SelectablePhoto
import page.angad.contacts.ui.main.displayName
import page.angad.contacts.util.appName
import page.angad.libcontacts.Contact
import page.angad.libcontacts.schema.RawContacts

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactListItem(
    contact: Contact,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    highlights: Set<Int> = emptySet()
) {
    val text = contact.displayName
    val annotated = buildAnnotatedString {
        text.forEachIndexed { idx, ch ->
            if (idx in highlights) {
                withStyle(SpanStyle(color = Color(0xffff6467))) { append(ch) }
                return@forEachIndexed
            }

            append(ch)
        }
    }

    SegmentedListItem(
        shapes = shapes,
        selected = selected,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SelectablePhoto(contact = contact, selected = selected)

            Text(
                annotated,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(Modifier.weight(1f))

            AccountLabel(
                contact = contact,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun AccountLabel(contact: Contact, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val account = contact[RawContacts].firstNotNullOfOrNull { it[RawContacts.AccountType] }

    Text(
        text = account?.let { appName(context, it) } ?: "Device",
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}
