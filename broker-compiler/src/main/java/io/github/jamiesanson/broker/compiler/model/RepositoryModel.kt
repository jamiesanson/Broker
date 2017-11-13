package io.github.jamiesanson.broker.compiler.model

import com.squareup.javapoet.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import com.squareup.javapoet.MethodSpec
import io.github.jamiesanson.broker.BrokerRepositoryManager
import io.github.jamiesanson.broker.compiler.util.Logger
import io.github.jamiesanson.broker.fulfillment.FulfillmentManager
import io.github.jamiesanson.broker.fulfillment.Provider

/**
 * Model of a repository annotated class
 */
data class RepositoryModel(
        private val repositoryElement: TypeElement,
        private val members: List<ExecutableElement>,
        private val logger: Logger
) {

    fun generateSpec(): TypeSpec.Builder {
        logger.log("Generating class: \"Broker${repositoryElement.simpleName}\"")

        // Create the repo class builder
        val repoSpec = TypeSpec.classBuilder("Broker${repositoryElement.simpleName}")
                .addSuperinterface(TypeName.get(repositoryElement.asType()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        repoSpec.addJavadoc(
                "Generated file for use with the Broker library. Retrieve\n" +
                "an instance of this class through the use of {@link \$T}.\n\nJamie Sanson 2017\n",
                BrokerRepositoryManager::class.java
        )

        // Add constructor
        val parameter = ParameterizedTypeName.get(Provider::class.java, FulfillmentManager::class.java)

        // Add constructor initializer
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameter, "managerProvider")
                .addStatement("this.\$N = \$N", "managerProvider", "managerProvider")

        repoSpec.addField(parameter, "managerProvider", Modifier.PRIVATE, Modifier.FINAL)

        // Add methods for each member
        for (member in members) {
            val methodModel = MethodModel(member, logger)
            val (method, field, initializer) = methodModel.generateSpec()

            repoSpec.addMethod(method)
            repoSpec.addField(field)
            constructor.addCode(initializer)
        }

        logger.log("Added ${members.size} members to \"Broker${repositoryElement.simpleName}\"")

        repoSpec.addMethod(constructor.build())
        return repoSpec
    }
}