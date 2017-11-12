package io.github.jamiesanson.broker.fulfillment

/**
 * Simple interface decoupling component provision
 */
interface Provider<out T> {

    fun get(): T
}