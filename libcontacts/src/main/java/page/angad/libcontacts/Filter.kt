package page.angad.libcontacts

/**
 * A selection predicate over fields of a single [Kind]. Build with [eq]/[inList] and
 * combine with [and]/[or]. Combining filters of different kinds does not compile.
 */
sealed interface Filter<K>

internal data class Eq<K, T>(val field: Field<K, T>, val value: T) : Filter<K>
internal data class InList<K, T>(val field: Field<K, T>, val values: Collection<T>) : Filter<K>
internal data class And<K>(val left: Filter<K>, val right: Filter<K>) : Filter<K>
internal data class Or<K>(val left: Filter<K>, val right: Filter<K>) : Filter<K>

/** `column = value`; a `null` value renders as `column IS NULL`. */
infix fun <K, T> Field<K, T>.eq(value: T): Filter<K> = Eq(this, value)

/** `column IN (...)`; an empty collection matches nothing. */
infix fun <K, T> Field<K, T>.inList(values: Collection<T>): Filter<K> = InList(this, values)

infix fun <K> Filter<K>.and(other: Filter<K>): Filter<K> = And(this, other)

infix fun <K> Filter<K>.or(other: Filter<K>): Filter<K> = Or(this, other)

/** Renders to a `selection` string and its ordered `selectionArgs`. */
internal fun Filter<*>.render(): Pair<String, List<String>> = when (this) {
    is Eq<*, *> ->
        if (value == null) "${field.column} IS NULL" to emptyList()
        else "${field.column} = ?" to listOf(field.type.arg(value))

    is InList<*, *> ->
        if (values.isEmpty()) "0" to emptyList()
        else "${field.column} IN (${List(values.size) { "?" }.joinToString(",")})" to
                values.map { field.type.arg(it) }

    is And<*> -> {
        val (l, la) = left.render()
        val (r, ra) = right.render()
        "($l) AND ($r)" to la + ra
    }

    is Or<*> -> {
        val (l, la) = left.render()
        val (r, ra) = right.render()
        "($l) OR ($r)" to la + ra
    }
}

/** The kind every field in this filter belongs to. */
@Suppress("UNCHECKED_CAST")
internal fun <K> Filter<K>.kind(): Kind<K> = when (this) {
    is Eq<*, *> -> field.kind
    is InList<*, *> -> field.kind
    is And<*> -> left.kind()
    is Or<*> -> left.kind()
} as Kind<K>
