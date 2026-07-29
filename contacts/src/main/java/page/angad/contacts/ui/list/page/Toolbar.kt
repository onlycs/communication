package page.angad.contacts.ui.list.page

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.DropdownMenuPopupPositionProvider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import dev.vicart.compose.material.symbols.FilledRoundedSymbol
import dev.vicart.compose.material.symbols.MaterialSymbols
import dev.vicart.compose.material.symbols.OutlinedRoundedSymbol
import kotlinx.coroutines.launch
import page.angad.contacts.ui.list.ContactListViewModel
import page.angad.contacts.util.LoadingCounter
import page.angad.contacts.util.attach
import page.angad.contacts.util.setFilename
import page.angad.libcontacts.Contact
import page.angad.libcontacts.inList
import page.angad.libcontacts.schema.Contacts
import page.angad.libcontacts.schema.RawContacts

@Composable
fun BoxScope.Toolbar(
    context: Context = LocalContext.current,
    viewModel: ContactListViewModel,
    selection: SnapshotStateMap<Long, Contact>,
    loading: LoadingCounter
) {
    val showDelete = remember { mutableStateOf(false) }

    DeleteDialog(
        showDelete,
        {
            viewModel.viewModelScope
                .launch {
                    viewModel.api
                        .delete(RawContacts)
                        .where(RawContacts.ContactId inList selection.keys.toList())
                        .commit()

                    selection.clear()
                    viewModel.reload()
                }
                .attach(loading)
        },
        selection.size == 1
    )

    AnimatedVisibility(
        visible = selection.isNotEmpty(),
        enter = slideInVertically(
            animationSpec = FloatingToolbarDefaults.animationSpec(),
            initialOffsetY = { it / 2 }
        ),
        exit = slideOutVertically(
            animationSpec = FloatingToolbarDefaults.animationSpec(),
            targetOffsetY = { 3 * it / 2 }
        ),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = { selection.clear() }
            ) {
                OutlinedRoundedSymbol(
                    MaterialSymbols.CHEVRON_LEFT,
                    size = 36.dp,
                )
            }

            HorizontalFloatingToolbar(expanded = true) {
                var shareMenu by remember { mutableStateOf(false) }
                BackHandler(shareMenu) { shareMenu = false }

                val shareMany = {
                    viewModel.viewModelScope
                        .launch {
                            val vCards = viewModel.api.vCards(selection.keys.toList())
                            if (vCards.isEmpty()) return@launch // TODO: show a toast?

                            val mime = vCards[0].mimeType
                            val uris =
                                vCards.map { setFilename(context, it.uri, it.suggestedFileName) }

                            val send = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = mime
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            context.startActivity(Intent.createChooser(send, "Export contacts"))
                        }
                        .attach(loading)
                }

                val shareOne = {
                    viewModel.viewModelScope
                        .launch {
                            val vCard = viewModel.api.vCardCombined(selection.keys.toList())
                                ?: return@launch // TODO: show a toast?

                            val mime = vCard.mimeType
                            val uri = setFilename(context, vCard.uri, vCard.suggestedFileName)

                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = mime
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            context.startActivity(Intent.createChooser(send, "Export contacts"))
                        }
                        .attach(loading)
                }

                DropdownMenuPopup(
                    expanded = shareMenu,
                    onDismissRequest = { shareMenu = false },
                    popupPositionProvider = positionDropdown(),
                ) {
                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShape(0, 1),
                        containerColor = MenuDefaults.groupVibrantContainerColor
                    ) {
                        DropdownMenuItem(
                            text = { Text("One file") },
                            colors = MenuDefaults.selectableItemVibrantColors(),
                            supportingText = {
                                Text(
                                    "Combine contacts into one file",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            shapes = MenuDefaults.itemShape(0, 2),
                            leadingIcon = {
                                @Suppress("DEPRECATION") // Non auto-mirrored is purposeful
                                Icon(Icons.Outlined.InsertDriveFile, null)
                            },
                            checked = false,
                            onCheckedChange = {
                                shareMenu = false
                                shareOne()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Many files") },
                            colors = MenuDefaults.selectableItemVibrantColors(),
                            supportingText = {
                                Text(
                                    "Each contact gets its own file",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            shapes = MenuDefaults.itemShape(1, 2),
                            leadingIcon = { OutlinedRoundedSymbol(MaterialSymbols.FILE_COPY) },
                            checked = false,
                            onCheckedChange = {
                                shareMenu = false
                                shareMany()
                            },
                        )
                    }
                }

                IconButton(onClick = {
                    if (selection.size == 1) shareMany()
                    else shareMenu = true
                }) {
                    OutlinedRoundedSymbol(MaterialSymbols.SHARE)
                }


                val allStarred = selection.isNotEmpty() && selection.values.all {
                    it[Contacts.Starred]
                }
                IconToggleButton(
                    checked = allStarred,
                    onCheckedChange = { star ->
                        viewModel.viewModelScope
                            .launch {
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
                            .attach(loading)
                    },
                    shapes = IconButtonDefaults.toggleableShapes(),
                    colors = IconButtonDefaults.iconToggleButtonColors().copy(
                        checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    if (allStarred) FilledRoundedSymbol(MaterialSymbols.STAR)
                    else OutlinedRoundedSymbol(MaterialSymbols.STAR)
                }


                val allSelected = selection.size == viewModel.list.size
                var preSelectAll by remember { mutableStateOf(emptyMap<Long, Contact>()) }
                IconToggleButton(
                    checked = allSelected,
                    onCheckedChange = { select ->
                        if (select) {
                            preSelectAll = selection.toMap()
                            selection += viewModel.map
                        } else {
                            selection.clear()
                            selection += preSelectAll.toMap()
                        }
                    },
                    shapes = IconButtonDefaults.toggleableShapes(),
                    colors = IconButtonDefaults.iconToggleButtonColors().copy(
                        checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    OutlinedRoundedSymbol(
                        if (allSelected) MaterialSymbols.DESELECT
                        else MaterialSymbols.SELECT_ALL,
                    )
                }

                IconButton(onClick = { showDelete.value = true }) {
                    OutlinedRoundedSymbol(MaterialSymbols.DELETE)
                }
            }
        }
    }
}

/**
 * Places the menu directly above its anchor. The stock menu position providers clamp to the
 * window minus its insets, which is above the floating toolbar in the gesture nav area.
 */
@Composable
private fun positionDropdown(
    shiftUp: Dp = 14.dp,
    shiftLeft: Dp = 8.dp
): DropdownMenuPopupPositionProvider {
    val upPx = with(LocalDensity.current) { shiftUp.roundToPx() }
    val leftPx = with(LocalDensity.current) { shiftLeft.roundToPx() }

    return remember(upPx) {
        object : DropdownMenuPopupPositionProvider {
            override val transformOrigin = TransformOrigin(0f, 1f)

            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ) = IntOffset(
                anchorBounds.left - leftPx,
                anchorBounds.top - popupContentSize.height - upPx
            )
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
        icon = { FilledRoundedSymbol(MaterialSymbols.DELETE) },
        title = {
            if (single) Text("Delete contact?")
            else Text("Delete contacts?")
        },
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