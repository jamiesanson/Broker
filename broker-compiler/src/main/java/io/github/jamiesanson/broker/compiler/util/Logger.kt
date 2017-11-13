package io.github.jamiesanson.broker.compiler.util

/**
 * Interface to be implemented by annotation processor
 */
interface Logger {
    fun log(log: String)
    fun warn(warning: String)
    fun error(error: String)
}