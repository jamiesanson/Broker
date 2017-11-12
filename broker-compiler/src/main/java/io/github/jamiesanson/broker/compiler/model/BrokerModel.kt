package io.github.jamiesanson.broker.compiler.model

import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.VariableElement

/**
 * Model representing a single field
 */
data class BrokerModel(
        val variable: VariableElement
): BaseModel {

    override fun generateSpec(): TypeSpec.Builder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}