package page.angad.contacts.ui.list.page

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import page.angad.contacts.ui.list.ContactListViewModel
import page.angad.contacts.util.setFilename
import page.angad.libcontacts.Contact
import page.angad.libcontacts.inList
import page.angad.libcontacts.schema.Contacts
import page.angad.libcontacts.schema.RawContacts

@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    viewModel: ContactListViewModel,
    selection: SnapshotStateMap<Long, Contact>,
) {
    val showDelete = remember { mutableStateOf(false) }

    DeleteDialog(
        showDelete,
        {
            viewModel.viewModelScope.launch {
                viewModel.api
                    .delete(RawContacts)
                    .where(RawContacts.ContactId inList selection.keys.toList())
                    .commit()

                selection.clear()
                viewModel.reload()
            }
        },
        selection.size == 1
    )

    AnimatedVisibility(
        visible = selection.isNotEmpty(),
        enter = slideInVertically(
            animationSpec = FloatingToolbarDefaults.animationSpec(),
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            animationSpec = FloatingToolbarDefaults.animationSpec(),
            targetOffsetY = { it * 2 }
        ),
        modifier = modifier
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
        ) {
            val context = LocalContext.current
            val allStarred = selection.isNotEmpty() && selection.values.all {
                it[Contacts.Starred]
            }

            IconButton(onClick = {
                viewModel.viewModelScope.launch {
                    val vCards = viewModel.api.vCards(selection.keys.toList())
                    val mime = vCards[0].mimeType
                    val uris = vCards.map { setFilename(context, it.uri, it.suggestedFileName) }

                    val send = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = mime
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(Intent.createChooser(send, "Export contacts"))
                }
            }) {
                Icon(Icons.Outlined.Share, "Share")
            }

            IconToggleButton(
                checked = allStarred,
                onCheckedChange = { star ->
                    viewModel.viewModelScope.launch {
                        val ids = selection.keys.toList()

                        viewModel.api
                            .update { it[Contacts.Starred] = star }
                            .where(Contacts.Id inList ids)
                            .commit()

                        viewModel.reload(ids)
                        ids.forEach { id ->
                            viewModel.map[id]?.let { selection[id] = it }
                        }
                    }
                },
                shapes = IconButtonDefaults.toggleableShapes(),
                colors = IconButtonDefaults.iconToggleButtonColors().copy(
                    checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    if (allStarred) Icons.Default.Star
                    else Icons.Default.StarBorder,
                    if (allStarred) "Unstar"
                    else "Star"
                )
            }

            IconButton(onClick = { showDelete.value = true }) {
                Icon(Icons.Outlined.Delete, "Delete")
            }
        }
    }
}

@Composable
fun DeleteDialog(
    visible: MutableState<Boolean>,
    onSuccess: () -> Unit,
    single: Boolean,
) {
    var visible by visible
    if (!visible) return

    AlertDialog(
        onDismissRequest = { visible = false },
        icon = { Icon(Icons.Default.Delete, contentDescription = "Delete") },
        title = { Text("Delete contact?") },
        text = {
            if (single) Text("This contact will be permanently deleted from your device")
            else Text("These contacts will be permanently deleted from your device")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    visible = false
                    onSuccess()
                }
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = { visible = false }) { Text("Cancel") }
        }
    )
}