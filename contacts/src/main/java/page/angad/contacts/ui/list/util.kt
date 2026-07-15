package page.angad.contacts.ui.list

import contacts.core.entities.Contact

const val STARRED_GROUP = '\u00a0'

fun Contact.sortGroup(): Char {
    return if (options?.starred ?: false) STARRED_GROUP
    else displayNamePrimary?.trim()?.takeIf { it.isNotEmpty() }?.uppercase()?.first() ?: '#'
}

data class GroupLabel(val group: Char, val startPx: Int, val endPx: Int)

data class ListGeometry(val prefix: List<Int>, val labels: List<GroupLabel>)

fun computeGeometry(
    groups: List<Pair<Char, Int>>,
    itemHt: Int,
    itemSp: Int,
    groupSp: Int,
): ListGeometry {
    val pfx = ArrayList<Int>(groups.sumOf { it.second } + groups.size)
    val labels = ArrayList<GroupLabel>(groups.size)
    var y = 0

    groups.forEachIndexed { gi, (group, size) ->
        pfx.add(y) // the gap item
        y += (if (gi == 0) 0 else groupSp) + itemSp
        val start = y
        repeat(size) {
            pfx.add(y)
            y += itemHt + itemSp
        }
        labels.add(GroupLabel(group, start, y - itemSp))
    }

    return ListGeometry(pfx, labels)
}