package page.angad.contacts.data.field

import android.content.Context
import page.angad.contacts.data.appName

sealed class Repository {
    data object Local : Repository()
    data class Online(val context: Context, val name: String, val pkgId: String) : Repository()

    override fun toString(): String {
        return when (this) {
            is Local -> "Local"
            is Online -> "$name (${appName(context, pkgId)}"
        }
    }

    fun online(): Online? {
        return this as? Online
    }

    companion object {
        fun from(context: Context, name: String?, pkgId: String?): Repository {
            return if (name != null && pkgId != null) {
                Online(context, name, pkgId)
            } else {
                Local
            }
        }
    }
}
