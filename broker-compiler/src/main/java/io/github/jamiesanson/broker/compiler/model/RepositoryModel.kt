package io.github.jamiesanson.broker.compiler.model

import com.squareup.javapoet.*
import io.github.jamiesanson.broker.annotation.Persistent
import io.github.jamiesanson.broker.annotation.Transient
import io.github.jamiesanson.broker.fulfillment.PersistenceType
import javax.annotation.processing.Messager
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import com.squareup.javapoet.MethodSpec
import io.github.jamiesanson.broker.annotation.ExpireDuration
import io.github.jamiesanson.broker.fulfillment.FulfillmentManager
import io.github.jamiesanson.broker.fulfillment.Provider
import org.joda.time.LocalDateTime

/**
 * Model of a repository annotated class
 */
data class RepositoryModel(
        private val repositoryElement: TypeElement,
        val members: List<ExecutableElement>,
        val messager: Messager
): BaseModel {

    override fun generateSpec(): TypeSpec.Builder {
        // Create the repo class builder
        val repoSpec = TypeSpec.classBuilder("Broker${repositoryElement.simpleName}")
                .addSuperinterface(TypeName.get(repositoryElement.asType()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        // Add constructor
        val parameter =  ParameterizedTypeName.get(
                ClassName.get(Provider::class.java),
                WildcardTypeName.subtypeOf(FulfillmentManager::class.java))

        val repoConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameter, "managerProvider")
                .addStatement("this.\$N = \$N", "managerProvider", "managerProvider")
                .build()

        repoSpec.addField(parameter, "managerProvider", Modifier.PRIVATE, Modifier.FINAL)
        repoSpec.addMethod(repoConstructor)

        // Add methods for each member
        for (member in members) {
            val methodSpec = MethodSpec.methodBuilder(member.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)

            // Inspect type parameters
            val returnClass = ParameterizedTypeName.get(member.returnType)

            log("Found return type: $returnClass")

            // Inspect annotation
            val paramMap: HashMap<String, Any> =
                    when {
                        member.getAnnotation(Persistent::class.java) != null -> member.getAnnotation(Persistent::class.java).getParamMap()
                        member.getAnnotation(Transient::class.java) != null -> member.getAnnotation(Transient::class.java).getParamMap()
                        else -> {
                            error("Expected Annotation not present")
                            throw IllegalStateException("Method not annotated with correct annotation")
                        }
                    }

            // Generate method
            with(methodSpec) {
                returns(returnClass)
                addCode(getMethodCodeBlock(paramMap, returnClass))
                addAnnotation(Override::class.java)
            }

            repoSpec.addMethod(methodSpec.build())
        }

        return repoSpec
    }

    private fun getMethodCodeBlock(paramMap: HashMap<String, Any>, returnType: TypeName): CodeBlock {
        val format = "return new \$type:T(" +
                "\$key:S," +
                "\$persistenceType:T.\$persistenceVal:L," +
                "new \$lastFetched:T(0)," +
                "new \$lastUpdated:T(0)," +
                "\$expirationType:T.\$expirationDuration:L.toDuration()," +
                "(\$managerType:T)\$managerProvider:N);"

        paramMap.put("type", returnType)
        paramMap.put("lastFetched", LocalDateTime::class.java)
        paramMap.put("lastUpdated", LocalDateTime::class.java)
        paramMap.put("expirationType", ExpireDuration::class.java)
        paramMap.put("persistenceType", PersistenceType::class.java)
        paramMap.put("managerType", ParameterizedTypeName.get(Provider::class.java, FulfillmentManager::class.java))
        paramMap.put("managerProvider", "this.managerProvider")

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

    private fun error(error: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, "[BROKER]: " + error)
    }

    private fun warn(warning: String) {
        messager.printMessage(Diagnostic.Kind.WARNING, "[BROKER]: " + warning)
    }

    private fun log(log: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, "[BROKER]: " + log)
    }
}