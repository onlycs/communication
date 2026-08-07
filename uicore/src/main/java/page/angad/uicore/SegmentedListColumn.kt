package page.angad.uicore

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed class ListItemPosition(val index: Int) {
    class Begin(index: Int) : ListItemPosition(index)
    class Middle(index: Int) : ListItemPosition(index)
    class End(index: Int) : ListItemPosition(index)
    class Solo(index: Int) : ListItemPosition(index)

    fun isEnd() = this is End || this is Solo
    fun isStart() = this is Begin || this is Solo

    fun cornerShape(shapes: Shapes): CornerBasedShape = when (this) {
        is Begin -> shapes.large.copy(
            bottomStart = shapes.extraSmall.bottomStart,
            bottomEnd = shapes.extraSmall.bottomEnd
        )

        is End -> shapes.large.copy(
            topStart = shapes.extraSmall.topStart,
            topEnd = shapes.extraSmall.topEnd
        )

        is Solo -> shapes.large
        is Middle -> shapes.extraSmall
    }

    companion object {
        fun from(i: Int, len: Int): ListItemPosition {
            if (len == 1) return Solo(i)
            return when (i) {
                0 -> Begin(i)
                len - 1 -> End(i)
                else -> Middle(i)
            }
        }
    }
}

open class ListItemData<T>(val value: T, val position: ListItemPosition) {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun shape(): ListItemShapes {
        val shapes = MaterialTheme.shapes
        val corners = remember(position, shapes) { position.cornerShape(shapes) }
        return ListItemDefaults.shapes(shape = corners)
    }
}

class GroupedListItemData<T, G>(value: T, val group: G, position: ListItemPosition) :
    ListItemData<T>(value, position)

@Composable
fun <T, G> SegmentedListColumn(
    modifier: Modifier = Modifier,
    content: @Composable (GroupedListItemData<T, G>) -> Unit,
    data: Iterable<T>,
    gap: @Composable (G) -> Unit = {},
    groupBy: (T, Int) -> G,
    itemId: ((T, G) -> Any)? = null,
    groupId: ((G) -> Any)? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    overscrollEffect: OverscrollEffect? = rememberOverscrollEffect(),
    state: LazyListState = rememberLazyListState(),
) {
    val groups = remember(data) {
        val grouped = mutableListOf<Pair<G, MutableList<T>>>()
        for ((i, item) in data.withIndex()) {
            val group = groupBy(item, i)
            if (grouped.isEmpty() || grouped.last().first != group) {
                grouped.add(group to mutableListOf(item))
            } else {
                grouped.last().second.add(item)
            }
        }
        grouped
    }

    LazyColumn(
        contentPadding = padding,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
        state = state,
        overscrollEffect = overscrollEffect
    ) {
        groups.forEach { (group, items) ->
            item(key = groupId?.invoke(group) ?: group) {
                gap(group)
            }

            itemsIndexed(
                items,
                key = { _, item -> itemId?.invoke(item, group) ?: item.hashCode() }
            ) { index, item ->
                content(
                    GroupedListItemData(
                        item,
                        group,
                        ListItemPosition.from(index, items.size)
                    )
                )
            }
        }
    }
}

@Composable
fun <T> SegmentedListColumn(
    modifier: Modifier = Modifier,
    content: @Composable (ListItemData<T>) -> Unit,
    data: Iterable<T>,
    id: ((T) -> Any)? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    overscrollEffect: OverscrollEffect? = rememberOverscrollEffect(),
    state: LazyListState = rememberLazyListState(),
) {
    val list = data.toList()

    LazyColumn(
        contentPadding = padding,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
        state = state,
        overscrollEffect = overscrollEffect
    ) {
        itemsIndexed(list, key = { i, item -> id?.let { it(item) } ?: i }) { i, item ->
            content(
                ListItemData(
                    item,
                    ListItemPosition.from(i, list.size)
                )
            )
        }
    }
}