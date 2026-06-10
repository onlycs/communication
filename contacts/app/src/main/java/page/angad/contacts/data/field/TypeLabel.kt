package page.angad.contacts.data.field

import android.content.Context
import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull

private val CDK_TYPE: String = "data2"
private val CDK_LABEL: String = "data3"

abstract class TypeParser<T : TypeLabel> : PartialFieldParser<T>() {
    private var _variants: List<T>? = null
    protected abstract val data: Class<*>

    fun collect(context: Context): List<T> =
        _variants ?: data.fields
            .filter { it.name.startsWith("TYPE_") && it.type == Int::class.java }
            .map { from(context, it.getInt(null)) }
            .also { _variants = it }

    override fun parse(context: Context, c: Cursor): T {
        val type = c.getIntOrNull(c.getColumnIndexOrThrow(CDK_TYPE))
        val label = c.getStringOrNull(c.getColumnIndexOrThrow(CDK_LABEL))

        return from(context, type, label)
    }

    protected abstract fun from(context: Context, type: Int?, label: String? = null): T
}

abstract class TypeLabel : FieldPartial() {
    abstract val type: Int
    abstract val label: String

    override fun write(context: Context, buffer: WriteBuffer) {
        buffer.put(CDK_TYPE, type)
        label.let { buffer.put(CDK_LABEL, it) }
    }
}