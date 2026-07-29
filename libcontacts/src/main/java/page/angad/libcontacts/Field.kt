package page.angad.libcontacts

import android.content.ContentValues
import android.database.Cursor
import kotlin.reflect.KType

/** A typed column of a [Kind]. Obtain instances via [Kind.field]. */
class Field<K, T>(val kind: Kind<K>, val column: String, val type: ColumnType) {
    val table: Table get() = kind.table

    override fun equals(other: Any?) =
        other is Field<*, *> && other.kind == kind && other.column == column

    override fun hashCode() = 31 * kind.hashCode() + column.hashCode()

    override fun toString() = "${kind::class.simpleName}.$column"
}

/** Storage class of a column, used to bridge cursor/ContentValues types. */
enum class ColumnType {
    LONG, INT, STRING, BOOLEAN
}

fun columnTypeOf(type: KType): ColumnType = when (type.classifier) {
    Long::class -> ColumnType.LONG
    Int::class -> ColumnType.INT
    String::class -> ColumnType.STRING
    Boolean::class -> ColumnType.BOOLEAN
    else -> throw IllegalArgumentException("Unsupported field type: $type")
}

internal fun ColumnType.read(c: Cursor, i: Int): Any? = if (c.isNull(i)) null else when (this) {
    ColumnType.LONG -> c.getLong(i)
    ColumnType.INT -> c.getInt(i)
    ColumnType.STRING -> c.getString(i)
    ColumnType.BOOLEAN -> c.getInt(i) != 0
}

internal fun ColumnType.put(values: ContentValues, column: String, value: Any?) {
    if (value == null) {
        values.putNull(column)
        return
    }

    when (this) {
        ColumnType.LONG -> values.put(column, value as Long)
        ColumnType.INT -> values.put(column, value as Int)
        ColumnType.STRING -> values.put(column, value as String)
        ColumnType.BOOLEAN -> values.put(column, if (value as Boolean) 1 else 0)
    }
}

/** Renders a value as a SQL selection argument. */
internal fun ColumnType.arg(value: Any?): String = when (this) {
    ColumnType.BOOLEAN -> if (value == true) "1" else "0"
    else -> value.toString()
}
