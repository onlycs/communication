package page.angad.contacts.ui.list

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import contacts.core.entities.Contact
import page.angad.contacts.util.dedupSz
import page.angad.uicore.SegmentedListColumn

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContactListBody(
    contacts: List<Contact>,
    starred: List<Contact>,
    selection: SnapshotStateSet<Long>
) {
    val density = LocalDensity.current
    val overscroll = rememberOverscrollEffect()
    val state = rememberLazyListState()

    val groups = remember(contacts) {
        listOf(STARRED_GROUP to starred.size) + contacts.map { it.sortGroup() }.dedupSz()
    }

    var itemHt by remember { mutableIntStateOf(0) }
    val itemSp = with(density) { 2.dp.roundToPx() }
    val groupSp = with(density) { 24.dp.roundToPx() }
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
            gap = { if (it != groups.firstOrNull()?.first) Spacer(Modifier.height(24.dp)) },
            content = { data ->
                ContactListItem(
                    data = data,
                    selected = data.value.id in selection,
                    onClick = {
                        if (selection.isEmpty()) {
                            // TODO: open contact
                        } else {
                            if (data.value.id in selection) selection -= data.value.id
                            else selection += data.value.id
                        }
                    },
                    onLongClick = {
                        if (selection.isEmpty()) selection += data.value.id
                    },
                    modifier = if (data.position.index == 0) {
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