package io.github.jamiesanson.broker

import io.github.jamiesanson.broker.annotation.internal.Resolver
import io.github.jamiesanson.broker.event.LifecycleEvent
import io.github.jamiesanson.broker.event.LifecycleEvent.Type.*
import io.github.jamiesanson.broker.event.SyncEvent
import io.github.jamiesanson.broker.event.SyncEvent.Type.*
import io.github.jamiesanson.broker.fulfillment.Fulfiller
import io.github.jamiesanson.broker.fulfillment.FulfillmentManager
import io.github.jamiesanson.broker.fulfillment.Provider
import io.github.jamiesanson.broker.util.RepoResolver
import io.github.jamiesanson.broker.util.ResolverInjector
import org.greenrobot.eventbus.EventBus

/**
 * Class to be initialised at the start of the application
 */
class BrokerRepositoryManager(val fulfiller: Fulfiller): Provider<FulfillmentManager> {

    private val fulfillmentManager = FulfillmentManager(fulfiller)

    @Resolver
    lateinit var resolver: RepoResolver

    init {
        ResolverInjector.inject(this)
    }

    override fun get(): FulfillmentManager {
        return fulfillmentManager
    }

    fun <T> getRepo(clazz: Class<T>): T {
        return resolver.get(clazz)
    }

    /**
     * Syncs all brokers with remotes if required
     */
    fun syncAll() {
        EventBus.getDefault().post(SyncEvent(IMMEDIATE))
    }

    /**
     * Must be called when repository is being stopped, forcing all brokers to unsubscribe from
     * EventBus messages
     */
    fun onDestroy() {
        EventBus.getDefault().post(LifecycleEvent(ON_DESTROY))
    }

    inner class Builder() {
        lateinit var fulfiller: Fulfiller

        fun fulfillment(fulfiller: Fulfiller): Builder {
            this.fulfiller = fulfiller
            return this
        }

        fun build(): BrokerRepositoryManager {
            return BrokerRepositoryManager(
                    fulfiller = this.fulfiller
            )
        }
    }
}