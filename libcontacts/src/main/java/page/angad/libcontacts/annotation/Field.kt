package page.angad.libcontacts.annotation

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import page.angad.libcontacts.utils.isDataClass
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Field(val cdk: KClass<*>)

class FieldProcessor(
    private val codegen: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val fields = resolver
            .getSymbolsWithAnnotation("page.angad.libcontacts.annotation.Field")
            .filterIsInstance<KSClassDeclaration>()

        val grouped = fields.groupBy { it.validate() && generate(it) }

        return grouped[false] ?: emptyList()
    }

    fun generate(field: KSClassDeclaration): Boolean {
        if (!field.isDataClass()) return false

        val tag = field.annotations.find { it.shortName.asString() == "Field" } ?: return false
        val cdkArg = tag.arguments.find { it.name?.asString() == "cdk" }?.value ?: return false
        val cdk = (cdkArg as? KSType)?.declaration as? KSClassDeclaration ?: return false

        return true
    }
}
