package io.github.jamiesanson.broker

import com.sun.org.apache.xml.internal.security.utils.ElementCheckerImpl
import io.github.jamiesanson.broker.event.SyncEvent
import io.github.jamiesanson.broker.event.SyncEvent.Type.*
import io.github.jamiesanson.broker.fulfillment.Fulfiller
import io.github.jamiesanson.broker.fulfillment.FulfillmentManager
import io.github.jamiesanson.broker.fulfillment.Provider
import org.greenrobot.eventbus.EventBus

/**
 * Class to be initialised at the start of the application
 */
class BrokerRepository(val fulfiller: Fulfiller): Provider<FulfillmentManager> {

    private val fulfillmentManager = FulfillmentManager(fulfiller)

    override fun get(): FulfillmentManager {
        return fulfillmentManager
    }

    fun syncAll() {
        EventBus.getDefault().post(SyncEvent(IMMEDIATE))
    }

    inner class Builder() {
        lateinit var fulfiller: Fulfiller

        fun fulfillment(fulfiller: Fulfiller): Builder {
            this.fulfiller = fulfiller
            return this
        }

        fun build(): BrokerRepository {
            return BrokerRepository(
                    fulfiller = this.fulfiller
            )
        }
    }
}