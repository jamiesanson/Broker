package io.github.jamiesanson.broker.compiler.model

import com.squareup.javapoet.*
import io.github.jamiesanson.broker.annotation.Persistent
import io.github.jamiesanson.broker.annotation.Transient
import io.github.jamiesanson.broker.compiler.util.Logger
import io.github.jamiesanson.broker.fulfillment.PersistenceType
import io.github.jamiesanson.broker.util.ExpireDuration
import org.joda.time.LocalDateTime
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

/**
 * Class representing individual member of repo
 */
class MethodModel(
        private val member: ExecutableElement,
        private val logger: Logger
) {

    /**
     * Generates the required specs for a given member.
     * This spec is a triplet, where the three type parameters
     * are get method spec, private field spec, and codeblock
     * for constructor initialization
     */
    fun generateSpec(makeLogs: Boolean): Triple<MethodSpec, FieldSpec, CodeBlock> {
        val returnClass = ParameterizedTypeName.get(member.returnType)
        val memberName = "${member.simpleName}Broker"

        if (makeLogs) {
            logger.log("Generating specs for $returnClass")
        }

        // Generate Field
        val field = FieldSpec.builder(returnClass, memberName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build()

        // Get constructor initializer
        val initializer = getConstructorCodeBlock(returnClass, memberName)

        // Generate Method
        val method = MethodSpec.methodBuilder(member.simpleName.toString())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Retrieves Broker for {@link \$L} named ${member.simpleName}\n",
                        returnClass.toString()
                        .substringAfter('<')
                        .substringBeforeLast('>'))
                .addAnnotation(Override::class.java)
                .returns(returnClass)
                .addCode("return \$N;\n", memberName)
                .build()

        return Triple(method, field, initializer)
    }

    /**
     * Function for forming constructor initializer
     */
    private fun getConstructorCodeBlock(returnType: TypeName, memberName: String): CodeBlock {
        // Inspect annotation
        val paramMap: HashMap<String, Any> =
                when {
                    member.getAnnotation(Persistent::class.java) != null -> member.getAnnotation(Persistent::class.java).getParamMap()
                    member.getAnnotation(Transient::class.java) != null -> member.getAnnotation(Transient::class.java).getParamMap()
                    else -> {
                        logger.error("Expected Annotation not present")
                        throw IllegalStateException()
                    }
                }

        // Format for Broker initialisation
        val format = "this.\$memberName:N = " +
                "new \$type:T(" +
                "\$key:S," +
                "\$persistenceType:T.\$persistenceVal:L," +
                "new \$lastFetched:T(0)," +
                "new \$lastUpdated:T(0)," +
                "\$expirationType:T.\$expirationDuration:L.toDuration()," +
                "\$managerProvider:N);\n"

        // Defaults for initialisation
        paramMap.put("type", returnType)
        paramMap.put("memberName", memberName)
        paramMap.put("lastFetched", LocalDateTime::class.java)
        paramMap.put("lastUpdated", LocalDateTime::class.java)
        paramMap.put("expirationType", ExpireDuration::class.java)
        paramMap.put("persistenceType", PersistenceType::class.java)
        paramMap.put("managerProvider", "this.managerProvider")

        // Build and return
        return CodeBlock.builder().addNamed(format, paramMap).build()
    }

    private fun Transient.getParamMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map.put("key", this.key)
        map.put("persistenceVal", PersistenceType.TRANSIENT.name)
        map.put("expirationDuration", ExpireDuration.FIVE_MINUTES.name)
        return map
    }

    private fun Persistent.getParamMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map.put("key", this.key)
        map.put("persistenceVal", this.type.name)
        map.put("expirationDuration", this.cacheExpireDuration.name)
        return map
    }
}
