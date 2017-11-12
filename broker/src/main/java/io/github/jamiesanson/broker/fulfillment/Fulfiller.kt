package io.github.jamiesanson.broker.fulfillment

/**
 * Interface to be implemented by consumer. Called by FulfillmentManager
 * off the main thread.
 */
interface Fulfiller {
    
    fun <T> getLocal(key: String): T 
    
    fun <T> getRemote(key: String): T
    
    fun <T> putLocal(key: String, value: T)
    
    fun <T> putRemote(key: String, value: T)
    
    fun existsLocal(key: String): Boolean
    
}