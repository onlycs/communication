package page.angad.contacts.ui.main

import page.angad.libcontacts.Contact
import page.angad.libcontacts.schema.Contacts

fun Contact.sortGroup(): Char {
    return this[Contacts.DisplayName]?.trim()?.takeIf { it.isNotEmpty() }?.uppercase()?.first()
        ?: '#'
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

val Contact.displayName: String
    get() = this[Contacts.DisplayName] ?: "(No name)"