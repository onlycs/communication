package page.angad.contacts.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun <T> Iterable<T>.dedup(eq: (T, T) -> Boolean = { a, b -> a == b }): List<T> {
    return dedupSz(eq).map { it.first }
}

fun <T> Iterable<T>.dedupSz(eq: (T, T) -> Boolean = { a, b -> a == b }): List<Pair<T, Int>> {
    var prv: Pair<T, Int>? = null
    val out = mutableListOf<Pair<T, Int>>()

    for (item in this) {
        if (prv == null) {
            prv = item to 1
            continue
        }

        if (eq(item, prv.first)) {
            prv = prv.first to prv.second + 1
            continue
        }

        out += prv
        prv = item to 1
    }

    prv?.let { out += it }

    return out
}

fun appName(context: Context, pkgId: String): String {
    try {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(pkgId, 0)
        return pm.getApplicationLabel(appInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        if (pkgId.contains('.')) return appName(
            context,
            pkgId.split('.').dropLast(1).joinToString(".")
        )

        return pkgId
    }
}

fun extractEmail(s: String): String {
    val regex = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    return regex.find(s)?.value ?: s
}

fun setFilename(context: Context, vCard: Uri, filename: String): Uri {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, filename)

    context.contentResolver.openInputStream(vCard)?.use { input ->
        FileOutputStream(file).use { output -> input.copyTo(output) }
    }
    
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}