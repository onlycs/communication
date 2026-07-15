package page.angad.contacts.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun LabelRail(
    geometry: ListGeometry,
    listState: LazyListState,
    itemHt: Int,
    topSp: Int,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    fun scrollPx(): Int {
        val i = listState.firstVisibleItemIndex
        return (geometry.prefix.getOrNull(i) ?: 0) + listState.firstVisibleItemScrollOffset
    }

    Box(
        modifier
            .width(64.dp)
            .fillMaxHeight()
            .clipToBounds()
    ) {
        if (itemHt > 0) {
            geometry.labels.forEach { label ->
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
                    GroupLabelContent(label.group)
                }
            }
        }
    }
}

@Composable
private fun GroupLabelContent(group: Char) {
    if (group == STARRED_GROUP) {
        Icon(
            Icons.Default.Star,
            contentDescription = "Starred",
            tint = MaterialTheme.colorScheme.primary
        )
    } else {
        Text(
            "$group",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}