package io.github.jamiesanson.broker.util

import io.github.jamiesanson.broker.annotation.internal.Resolver
import io.github.jamiesanson.broker.fulfillment.FulfillmentManager
import io.github.jamiesanson.broker.fulfillment.Provider

/**
 * Injector to attempt to use injection to instantiate field of RepositoryManager
 */
class ResolverInjector {

    companion object {
        @JvmField
        val generatedResolverClass = Class.forName("io.github.jamiesanson.broker.compiler.BrokerRepoResolver")!!

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun inject(obj: Any) {
            val fields = obj::class.java.declaredFields
            for (field in fields) {
                if (field.isAnnotationPresent(Resolver::class.java)) {
                    field.isAccessible = true

                    try {
                        val constr = generatedResolverClass.getConstructor(Provider::class.java)
                        val repoResolver = constr.newInstance(obj as Provider<FulfillmentManager>)
                        field.set(obj, repoResolver)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}