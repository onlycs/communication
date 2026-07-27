package page.angad.libcontacts

/** A single `column = value` write, type-checked against the field's [T]. */
data class Assignment<K : Kind<K>, T>(val field: Field<K, T>, val value: T)

/** Collects assignments for a single kind [K]; mixing kinds does not compile. */
class AssignmentScope<K : Kind<K>> {
    internal val assignments = mutableListOf<Assignment<K, *>>()

    operator fun <T> set(field: Field<K, T>, value: T) {
        assignments += Assignment(field, value)
    }
}
