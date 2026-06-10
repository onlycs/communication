package page.angad.contacts.data.field

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.Context
import android.provider.ContactsContract
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import page.angad.contacts.data.Cell
import page.angad.contacts.data.cell

abstract class BaseField {
    protected class WriteBuffer(delegate: Cell<ContentProviderOperation.Builder>) {
        private var op by delegate

        fun put(key: String, value: String) {
            op = op.withValue(key, value)
        }

        fun put(key: String, value: Int) {
            op = op.withValue(key, value)
        }

        fun put(context: Context, field: FieldPartial) {
            field.write(context, this)
        }
    }

    sealed class Operation<Res>(op: ContentProviderOperation.Builder) {
        protected val delegate = cell(op)
        private val buf = WriteBuffer(delegate)
        protected var op by delegate

        abstract fun apply(context: Context): Res

        protected fun write(context: Context, field: Field) {
            field.write(context, buf)
        }

        sealed class OperationComposable(op: ContentProviderOperation.Builder) :
            Operation<ContentProviderResult>(op)
        private typealias OperationMany = Operation<List<ContentProviderResult>>

        class Update(val field: Field) : OperationComposable(
            ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                .withSelection("${Data._ID} = ?", arrayOf(field.id.toString()))
        ) {
            override fun apply(context: Context): ContentProviderResult {
                write(context, field)
                return context.contentResolver.applyBatch(
                    ContactsContract.AUTHORITY,
                    arrayListOf(this.op.build())
                )[0]
            }
        }

        class Create(val field: Field, contactId: Long) : OperationComposable(
            ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValue(Data.RAW_CONTACT_ID, contactId)
                .withValue(Data.MIMETYPE, field.mime)
        ) {
            override fun apply(context: Context): ContentProviderResult {
                write(context, field)
                return context.contentResolver.applyBatch(
                    ContactsContract.AUTHORITY,
                    arrayListOf(this.op.build())
                )[0]
            }
        }

        class Delete(val id: Long) : OperationComposable(
            ContentProviderOperation.newDelete(Data.CONTENT_URI)
                .withSelection("${Data._ID} = ?", arrayOf(id.toString()))
        ) {
            override fun apply(context: Context): ContentProviderResult {
                return context.contentResolver.applyBatch(
                    ContactsContract.AUTHORITY,
                    arrayListOf(this.op.build())
                )[0]
            }
        }

        class Many(val operations: List<OperationComposable>) :
            OperationMany(ContentProviderOperation.newInsert(Data.CONTENT_URI)) {
            override fun apply(context: Context): List<ContentProviderResult> {
                val ops = ArrayList(operations.map {
                    when (it) {
                        is Update -> it.write(context, it.field)
                        is Create -> it.write(context, it.field)
                        is Delete -> {}
                    }

                    it.op.build()
                })

                return context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops).toList()
            }
        }

        class NewContact(val fields: List<Field>, val repo: Repository) :
            OperationMany(ContentProviderOperation.newInsert(Data.CONTENT_URI)) {
            override fun apply(context: Context): List<ContentProviderResult> {
                val ops = arrayListOf(run {
                    val online = repo.online()
                    ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                        .withValue(RawContacts.ACCOUNT_NAME, online?.name)
                        .withValue(RawContacts.ACCOUNT_TYPE, online?.pkgId)
                        .build()
                })

                for (field in fields) {
                    this.op = ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                        .withValue(Data.MIMETYPE, field.mime)

                    write(context, field)
                    ops += this.op.build()
                }

                return context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops).toList()
            }
        }
    }

    protected abstract fun write(context: Context, buffer: WriteBuffer)
}

abstract class FieldPartial : BaseField()

abstract class Field(val mime: String) : BaseField() {
    abstract val id: Long
}

class Operation {
    typealias Update = BaseField.Operation.Update
    typealias Create = BaseField.Operation.Create
    typealias Many = BaseField.Operation.Many
    typealias NewContact = BaseField.Operation.NewContact
    typealias Delete = BaseField.Operation.Delete
}