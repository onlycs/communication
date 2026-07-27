package page.angad.libcontacts

import kotlin.reflect.typeOf

/**
 * A named group of fields queried together: one provider table, or one Data mimetype.
 * [K] is the concrete kind object itself, which scopes filters and sub-row access to
 * a single kind at compile time.
 */
abstract class Kind<K : Kind<K>>(val table: Table) {
    /** Primary key of this kind's rows; always selected. */
    @Suppress("PropertyName")
    abstract val Id: Field<K, Long>

    /** The aggregate contact a row belongs to; always selected. */
    @Suppress("PropertyName")
    abstract val ContactId: Field<K, Long>

    // Safe by construction: every kind is declared as `object X : Kind<X>`.
    @Suppress("UNCHECKED_CAST")
    protected inline fun <reified T> field(column: String): Field<K, T> =
        Field(this as K, column, columnTypeOf(typeOf<T>()))
}
