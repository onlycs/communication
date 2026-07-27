package page.angad.libcontacts

enum class SortDirection { ASC, DESC }

/** One sort key of an `orderBy`; build with [asc]/[desc]. */
data class OrderSpec<K>(
    val field: Field<K, *>,
    val direction: SortDirection = SortDirection.ASC,
    val ignoreCase: Boolean = false,
)

fun <K> Field<K, *>.asc(ignoreCase: Boolean = false) = OrderSpec(this, SortDirection.ASC, ignoreCase)

fun <K> Field<K, *>.desc(ignoreCase: Boolean = false) = OrderSpec(this, SortDirection.DESC, ignoreCase)

internal fun List<OrderSpec<*>>.render(): String = joinToString(", ") {
    val collate = if (it.ignoreCase) " COLLATE NOCASE" else ""
    "${it.field.column}$collate ${it.direction.name}"
}
