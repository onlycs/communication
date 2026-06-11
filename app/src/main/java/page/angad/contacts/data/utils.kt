package page.angad.contacts.data

import android.content.Context
import android.content.pm.PackageManager
import kotlin.reflect.KProperty

class Cell<T>(var value: T) {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): T = value
    operator fun setValue(thisRef: Any?, prop: KProperty<*>, new: T) {
        value = new
    }
}

fun appName(context: Context, pkgId: String): String {
    try {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(pkgId, 0)
        return pm.getApplicationLabel(appInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        return pkgId
    }
}

fun <T> cell(initial: T) = Cell(initial)