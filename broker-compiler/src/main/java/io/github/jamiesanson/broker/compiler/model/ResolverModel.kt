package io.github.jamiesanson.broker.compiler.model

import com.squareup.javapoet.*
import io.github.jamiesanson.broker.compiler.util.Logger
import io.github.jamiesanson.broker.fulfillment.FulfillmentManager
import io.github.jamiesanson.broker.fulfillment.Provider
import io.github.jamiesanson.broker.util.RepoResolver
import java.lang.ClassCastException
import java.util.ArrayList
import javax.lang.model.element.Modifier

/**
 * Model for generating spec for resolver
 */
class ResolverModel(
        private val repos: List<RepositoryModel>,
        private val logger: Logger
) {

    fun generateSpec(): TypeSpec.Builder {
        // Create the repo resolver class builder
        logger.log("Generating resolver class")
        val repoSpec = TypeSpec.classBuilder("Broker${RepoResolver::class.java.simpleName}")
                .addSuperinterface(RepoResolver::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        // Generate constructor
        val parameter = ParameterizedTypeName.get(Provider::class.java, FulfillmentManager::class.java)
        val paramName = "managerProvider"
        val fieldName = "repositoryList"

        val repoField = FieldSpec.builder(ParameterizedTypeName.get(List::class.java, Object::class.java), fieldName)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new \$T()", ArrayList::class.java)
                .build()

        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameter, paramName)
                .addCode(getRepositoryInitializerCodeBlock(repoField, paramName))
                .build()

        repoSpec.addField(repoField)
        repoSpec.addMethod(constructor)

        // Generate repository getter
        repoSpec.addMethod(getGetterMethodSpec(repoField))

        return repoSpec
    }

    private fun getGetterMethodSpec(field: FieldSpec): MethodSpec {
        val typedVar = TypeVariableName.get("T")
        val parameterName = "classToFind"
        val tempParamName = "repo"

        // Generate spec builder
        val spec = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override::class.java)
                .returns(typedVar)
                .addTypeVariable(typedVar)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class::class.java), typedVar), parameterName)
                .beginControlFlow("for (\$T \$L: \$N)", Object::class.java, tempParamName, field)
                .addCode("try {\n")
                .addStatement("\t\$T temp = (\$T) \$L", typedVar, typedVar, tempParamName)
                .addStatement("\treturn temp")
                .addCode("} catch (\$T \$L) {\n", ClassCastException::class.java, "e")
                .addStatement("\tcontinue")
                .addCode("}\n")
                .endControlFlow()
                .addStatement("return null")

        return spec.build()
    }


    private fun getRepositoryInitializerCodeBlock(field: FieldSpec, fulfillmentProvider: String): CodeBlock {
        val builder = CodeBlock.builder()
        repos
                .map { ClassName.get("io.github.jamiesanson.broker.compiler", it.generateSpec(false).build().name) }
                .forEach { builder.add("\$N.add(new \$T(\$L));\n", field, it, fulfillmentProvider) }

        return builder.build()
    }
}