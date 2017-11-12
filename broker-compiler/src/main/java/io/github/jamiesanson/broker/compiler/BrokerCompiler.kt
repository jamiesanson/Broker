package io.github.jamiesanson.broker.compiler

import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion
import io.github.jamiesanson.broker.annotation.BrokerRepo
import javax.tools.Diagnostic
import javax.lang.model.element.ElementKind
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.collections.HashMap


/**
 * Class responsible for compiling Broker annotated fields and
 * `@BrokerRepo` annotated classes
 */
class BrokerCompiler: AbstractProcessor() {

    private lateinit var messager: Messager
    private lateinit var typesUtil: Types
    private lateinit var elementsUtil: Elements
    private lateinit var filer: Filer

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
        typesUtil = processingEnv.typeUtils
        elementsUtil = processingEnv.elementUtils
        filer = processingEnv.filer
    }

    override fun process(p0: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        val repoClasses: HashMap<String, String> = HashMap()

        // Find all annotated classes
        for (element in roundEnvironment!!.getElementsAnnotatedWith(BrokerRepo::class.java)) {
            if (element.kind !== ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "BrokerRepo can only be applied to a class.")
                return true
            }

            val typeElement = element as TypeElement

            repoClasses.put(
                    typeElement.simpleName.toString(),
                    elementsUtil.getPackageOf(typeElement).qualifiedName.toString())
        }

        return false
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return Collections.emptySet()
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8
    }
}