package page.angad.contacts.data.field

import android.content.ContentUris
import android.content.Context
import arrow.core.raise.either
import arrow.core.raise.ensure
import page.angad.contacts.data.Contact

private sealed class LazyManual<T> {
    class Some<T>(val value: T) : LazyManual<T>()
    class None<T> : LazyManual<T>()
}

class FieldOne<T : Field>(
    private val parser: FieldParser<T>,
    private val context: Context,
    private val contact: Contact,
) {
    sealed class Error {
        data object NotLoaded : Error()
        data object AlreadyExists : Error()
        data object Empty : Error()
    }

    private var _field: LazyManual<T?> = LazyManual.None()

    var field: T?
        get() = when (val f = _field) {
            is LazyManual.Some -> f.value
            is LazyManual.None -> query()
        }
        private set(value) {
            _field = LazyManual.Some(value)
        }

    val isLoaded: Boolean
        get() = _field is LazyManual.Some

    private fun query(): T? {
        val q = parser.queryOne(context, contact.id)
        _field = LazyManual.Some(q)
        return q
    }

    fun reload() {
        query()
    }

    fun write() = either {
        ensure(isLoaded) { Error.NotLoaded }
        ensure(field != null) { Error.Empty }

        Operation.Update(field!!).apply(context)
        query()
    }

    fun create(new: T) = either {
        ensure(isLoaded) { Error.NotLoaded }
        ensure(field == null) { Error.AlreadyExists }

        Operation.Create(new, contact.id).apply(context)
        query()
    }

    fun delete() = either {
        ensure(isLoaded) { Error.NotLoaded }
        ensure(field != null) { Error.Empty }

        Operation.Delete(field!!.id).apply(context)
        query()
    }
}

class FieldMany<T : Field>(
    private val parser: FieldParser<T>,
    private val context: Context,
    private val contact: Contact,
) {
    sealed class Error {
        data object NotLoaded : Error()
        data class NotFound(val id: Long) : Error()
    }

    private var _fields: LazyManual<MutableList<T>> = LazyManual.None()

    val fields: List<T>
        get() = when (val f = _fields) {
            is LazyManual.Some -> f.value
            is LazyManual.None -> query()
        }

    private var fieldsWrite: MutableList<T>
        get() = when (val f = _fields) {
            is LazyManual.Some -> f.value
            is LazyManual.None -> query()
        }
        set(value) {
            _fields = LazyManual.Some(value)
        }

    val isLoaded: Boolean
        get() = _fields is LazyManual.Some

    private fun query(): MutableList<T> {
        val q = parser.query(context, contact.id)
        _fields = LazyManual.Some(q)
        return q
    }

    fun reload() {
        query()
    }

    private fun query(id: Long, i: Int) = either {
        val q = parser.queryBy(context, id)

        ensure(q != null) {
            fieldsWrite.removeAt(i)
            Error.NotFound(id)
        }

        fieldsWrite[i] = q
        q
    }.map { it!! }

    private fun fieldBy(id: Long) = either {
        ensure(isLoaded) { Error.NotLoaded }

        val i = fields.indexOfFirst { it.id == id }
        ensure(i != -1) { Error.NotFound(id) }

        Pair(fields[i], i)
    }

    fun write(id: Long) = either {
        val (field, i) = fieldBy(id).bind()

        Operation.Update(field).apply(context)
        query(id, i).bind()
    }

    fun write(ids: List<Long>) = either {
        ensure(isLoaded) { Error.NotLoaded }

        val (fields, idxs) = ids.map { id -> fieldBy(id).bind() }.unzip()
        Operation.Many(fields.map { Operation.Update(it) }).apply(context)

        idxs.sorted().reversed().map { query(fields[it].id, it) }
    }

    fun delete(id: Long) = either {
        val (field, i) = fieldBy(id).bind()

        Operation.Delete(field.id).apply(context)
        fieldsWrite.removeAt(i)
        field
    }

    fun delete(ids: List<Long>) = either {
        ensure(isLoaded) { Error.NotLoaded }

        val (fields, idxs) = ids.map { id -> fieldBy(id).bind() }.unzip()
        Operation.Many(fields.map { Operation.Delete(it.id) }).apply(context)

        val res = idxs.map { fields[it] }
        idxs.sorted().reversed().forEach { fieldsWrite.removeAt(it) }

        res
    }

    fun add(new: T): T {
        val res = Operation.Create(new, contact.id).apply(context)
        val id = ContentUris.parseId(res.uri!!)
        val q = parser.queryBy(context, id)!!
        fieldsWrite += q // even if not init, we pull by reading

        return q
    }

    fun add(new: List<T>): List<T> {
        val res = Operation.Many(new.map { Operation.Create(it, contact.id) }).apply(context)
        val ids = res.map { ContentUris.parseId(it.uri!!) }
        val qs = ids.map { parser.queryBy(context, it)!! }
        fieldsWrite += qs // even if not init, we pull by reading

        return qs
    }
}

fun <T : Field> one(parser: FieldParser<T>, context: Context, contact: Contact) =
    FieldOne(parser, context, contact)

fun <T : Field> many(parser: FieldParser<T>, context: Context, contact: Contact) =
    FieldMany(parser, context, contact)