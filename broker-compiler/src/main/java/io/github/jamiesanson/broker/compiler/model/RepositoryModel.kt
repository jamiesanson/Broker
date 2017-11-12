package io.github.jamiesanson.broker.compiler.model

import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * Model of a repository annotated class
 */
data class RepositoryModel(
        val repositoryElement: TypeElement,
        val members: List<VariableElement>
): BaseModel {

    override fun generateSpec(): TypeSpec.Builder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}