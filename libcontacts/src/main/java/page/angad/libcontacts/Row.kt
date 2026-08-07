package page.angad.libcontacts

/** One row of a [Kind], always carrying [id] and [contactId]. */
class Row<K> internal constructor(
    internal val kind: Kind<K>,
    private val values: Map<Field<K, *>, Any?>,
) {
    /** Primary key of this row within its own kind. */
    val id: Long get() = get(kind.Id)

    /** The aggregate contact this row belongs to. */
    val contactId: Long get() = get(kind.ContactId)

    /** Whether [field] was part of the select projection, and so readable via [get]. */
    operator fun contains(field: Field<K, *>): Boolean = field in values

    /** Returns the value of [field]; throws if [field] was not part of the select projection. */
    operator fun <T> get(field: Field<K, T>): T {
        require(field in values) { "$field was not selected" }
        @Suppress("UNCHECKED_CAST")
        return values[field] as T
    }

    override fun equals(other: Any?) =
        other is Row<*> && other.kind == kind && other.values == values

    override fun hashCode() = 31 * kind.hashCode() + values.hashCode()

    override fun toString() =
        "Row(${values.entries.joinToString { "${it.key.column}=${it.value}" }})"
}
