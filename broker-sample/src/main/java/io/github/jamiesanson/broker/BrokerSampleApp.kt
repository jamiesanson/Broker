package io.github.jamiesanson.broker

import android.app.Application
import android.content.Context
import io.github.jamiesanson.broker.fulfillment.Fulfiller
import io.github.jamiesanson.broker.fulfillment.FulfillmentManager
import io.github.jamiesanson.broker.fulfillment.Provider

class BrokerSampleApp: Application(), Provider<FulfillmentManager> {

    override fun get(): FulfillmentManager {
        return FulfillmentManager(SampleFulfiller(this))
    }

    inner class SampleFulfiller(context: Context): Fulfiller {

        override fun <T> getLocal(key: String): T {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun <T> getRemote(key: String): T {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun <T> putLocal(key: String, value: T) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun <T> putRemote(key: String, value: T) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun existsLocal(key: String): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

}