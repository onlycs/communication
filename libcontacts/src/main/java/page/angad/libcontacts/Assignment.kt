package page.angad.libcontacts

/** A single `column = value` write, type-checked against the field's [T]. */
data class Assignment<T>(val field: Field<*, T>, val value: T)

class AssignmentScope {
    internal val assignments = mutableListOf<Assignment<*>>()

    operator fun <T> set(field: Field<*, T>, value: T) {
        assignments += Assignment(field, value)
    }
}
