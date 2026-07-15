package page.angad.contacts.ui.contacts

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.withoutVisualEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import contacts.core.entities.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import page.angad.contacts.util.appName
import page.angad.contacts.util.dedupSz
import page.angad.uicore.SegmentedListColumn
import kotlin.math.max
import kotlin.math.min

private fun Contact.sortGroup(): Char {
    return if (options?.starred ?: false) '\u00a0'
    else displayNamePrimary?.trim()?.takeIf { it.isNotEmpty() }?.uppercase()?.first() ?: '#'
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLeading(scope: CoroutineScope, state: SearchBarState) {
    when (state.currentValue) {
        SearchBarValue.Expanded -> {
            IconButton(onClick = { scope.launch { state.animateToCollapsed() } }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }

        SearchBarValue.Collapsed -> {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTrailing(state: SearchBarState) {
    if (state.currentValue == SearchBarValue.Collapsed) {
        IconButton(onClick = { /* menu side panel thingy */ }) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactsScreen(context: Context = LocalContext.current) {
    val inputState = rememberTextFieldState()
    val barState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val viewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(context)
    )

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            leadingIcon = { SearchLeading(scope, barState) },
            trailingIcon = { SearchTrailing(barState) },
            searchBarState = barState,
            textFieldState = inputState,
            onSearch = { scope.launch { barState.animateToExpanded() } },
            placeholder = { Text("Search contacts") },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                AppBarWithSearch(
                    state = barState,
                    inputField = inputField,
                    colors = SearchBarDefaults.appBarWithSearchColors(
                        appBarContainerColor = Color.Transparent
                    )
                )
                ExpandedFullScreenSearchBar(
                    state = barState,
                    inputField = inputField
                ) {
                    // search results
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.extraLarge.copy(
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp)
            )
        ) {
            val density = LocalDensity.current
            val overscroll = rememberOverscrollEffect()
            val state = rememberLazyListState()

            val items = viewModel.list
            val groups = remember(items) { items.map { it.sortGroup() }.dedupSz() }

            var itemHt by remember { mutableIntStateOf(0) }
            val itemSp = with(density) { 2.dp.roundToPx() }
            val groupSp = with(density) { 24.dp.roundToPx() }
            val topSp = with(density) { 16.dp.roundToPx() }

            class GroupLabel(val group: Char, val startPx: Int, val endPx: Int)

            val layout = remember(groups, itemHt, itemSp, groupSp) {
                val pfx = ArrayList<Int>(groups.sumOf { it.second } + groups.size)
                val labels = ArrayList<GroupLabel>(groups.size)
                var y = 0

                groups.forEachIndexed { gi, (group, size) ->
                    pfx.add(y)                                    // the gap item
                    y += (if (gi == 0) 0 else groupSp) + itemSp
                    val start = y
                    repeat(size) {
                        pfx.add(y)
                        y += itemHt + itemSp
                    }
                    labels.add(GroupLabel(group, start, y - itemSp))
                }

                pfx to labels
            }

            fun scrollPx(): Int {
                val (prefix, _) = layout
                val i = state.firstVisibleItemIndex
                return (prefix.getOrNull(i) ?: 0) + state.firstVisibleItemScrollOffset
            }

            Row(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (overscroll != null) Modifier.overscroll(overscroll)
                        else Modifier
                    )
            ) {
                Box(
                    Modifier
                        .width(64.dp)
                        .fillMaxHeight()
                        .clipToBounds()
                ) {
                    if (itemHt > 0) {
                        layout.second.forEach { label ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(with(density) { itemHt.toDp() })
                                    .offset {
                                        val scroll = scrollPx()
                                        val desired = topSp + label.startPx - scroll
                                        val pushedUp = topSp + label.endPx - itemHt - scroll
                                        IntOffset(0, min(max(desired, topSp), pushedUp))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (label.group == '\u00a0') {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Starred",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        "${label.group}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                SegmentedListColumn(
                    data = items,
                    groupBy = { it.sortGroup() },
                    itemId = { it.id },
                    groupId = { it },
                    padding = PaddingValues(
                        top = 16.dp,
                        bottom = 16.dp,
                        end = 16.dp,
                        start = 4.dp
                    ),
                    gap = { if (it != groups.firstOrNull()?.first) Spacer(Modifier.height(24.dp)) },
                    content = { data ->
                        SegmentedListItem(
                            onClick = {},
                            shapes = data.shape(),
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            modifier = when {
                                data.position.index == 0 -> {
                                    Modifier.onSizeChanged { px ->
                                        if (px.height > 0 && px.height != itemHt) itemHt =
                                            px.height
                                    }
                                }

                                data.position.isStart() -> {
                                    Modifier.padding(top = 24.dp)
                                }

                                else -> Modifier
                            }
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

                                Box(Modifier.padding(start = 16.dp)) {
                                    val account =
                                        data.value.rawContacts.find { it.account != null }?.account

                                    if (account == null) {
                                        Text(
                                            "Device",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall
                                        )

                                        return@Box
                                    }

                                    Text(
                                        appName(context, account.type),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    },
                    state = state,
                    overscrollEffect = overscroll?.withoutVisualEffect(),
                )
            }
        }

    }
}