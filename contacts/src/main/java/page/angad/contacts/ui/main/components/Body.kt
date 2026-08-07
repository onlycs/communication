package page.angad.contacts.ui.main.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.withoutVisualEffect
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import page.angad.contacts.ui.main.ContactListIntent
import page.angad.contacts.ui.main.ContactListState
import page.angad.contacts.ui.main.computeGeometry
import page.angad.contacts.ui.main.sortGroup
import page.angad.contacts.util.runs
import page.angad.libcontacts.Contact
import page.angad.uicore.SegmentedListColumn

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactListBody(state: ContactListState = ContactListState.current) {
    val contacts = state.viewModel.list
    val starred = state.viewModel.starred

    val density = LocalDensity.current
    val overscroll = rememberOverscrollEffect()
    val state = rememberLazyListState()

    val groups = remember(contacts) {
        val star = if (starred.isEmpty()) emptyList() else listOf(STARRED_GROUP to starred.size)
        star + contacts.map { it.sortGroup() }.runs()
    }

    var itemHt by remember { mutableIntStateOf(0) }
    val itemSp = with(density) { 2.dp.roundToPx() }
    val groupSp = with(density) { 18.dp.roundToPx() }
    val topSp = with(density) { 16.dp.roundToPx() }

    val geometry = remember(groups, itemHt, itemSp, groupSp) {
        computeGeometry(groups, itemHt, itemSp, groupSp)
    }

    Row(
        Modifier
            .fillMaxSize()
            .then(
                if (overscroll != null) Modifier.overscroll(overscroll)
                else Modifier
            )
    ) {
        LabelRail(
            geometry = geometry,
            listState = state,
            itemHt = itemHt,
            topSp = topSp,
        )

        SegmentedListColumn(
            data = starred + contacts,
            groupBy = { it, i -> if (i < starred.size) STARRED_GROUP else it.sortGroup() },
            itemId = { it, group -> it.id to group },
            groupId = { it },
            padding = PaddingValues(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 4.dp),
            gap = { if (it != groups.firstOrNull()?.first) Spacer(Modifier.height(18.dp)) },
            content = {
                SegmentedContactList(
                    it.value,
                    it.shape(),
                    if (it.position.index == 0) {
                        Modifier.onSizeChanged { px ->
                            if (px.height > 0 && px.height != itemHt) itemHt = px.height
                        }
                    } else Modifier
                )
            },
            state = state,
            overscrollEffect = overscroll?.withoutVisualEffect(),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchedContactList(matching: List<Pair<Contact, Set<Int>>>) {
    val state = rememberLazyListState()

    SegmentedListColumn(
        data = matching,
        padding = PaddingValues(bottom = 16.dp, end = 16.dp, start = 16.dp),
        content = {
            SegmentedContactList(
                it.value.first,
                it.shape(),
                highlights = it.value.second
            )
        },
        state = state,
    )
}

@Composable
private fun SegmentedContactList(
    contact: Contact,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    highlights: Set<Int> = emptySet(),
    state: ContactListState = ContactListState.current
) {
    val (viewModel, selection, loading, intent) = state
    val id = contact.id

    ContactListItem(
        contact = contact,
        shapes = shapes,
        selected = id in selection,
        onClick = {
            when (intent) {
                is ContactListIntent.Ui -> when {
                    selection.isEmpty() -> {} // TODO: open contact
                    else -> {
                        if (id in selection) selection -= id
                        else selection += id to contact
                    }
                }

                is ContactListIntent.Pick -> {
                    viewModel.launch(loading) {
                        intent.resolve(contact)
                    }
                }
            }
        },
        onLongClick = if (selection.isEmpty() && intent is ContactListIntent.Ui) ({ selection += id to contact }) else null,
        modifier = modifier,
        highlights = highlights
    )
}