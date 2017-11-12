package io.github.jamiesanson.broker.compiler.model

import com.squareup.javapoet.TypeSpec

/**
 * Base interface of a generatable model
 */
interface BaseModel {

    /**
     * Function for generating class spec
     */
    fun generateSpec(): TypeSpec.Builder
}