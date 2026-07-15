package page.angad.libcontacts.utils

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

fun KSClassDeclaration.isDataClass(): Boolean {
    return classKind == ClassKind.CLASS && Modifier.DATA in modifiers
}