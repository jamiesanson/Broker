package io.github.jamiesanson.broker

import android.app.Application
import android.content.Context
import android.util.Log
import io.github.jamiesanson.broker.fulfillment.Fulfiller

class BrokerSampleApp: Application() {

    lateinit var repoManager: BrokerRepositoryManager

    override fun onCreate() {
        super.onCreate()
        repoManager = BrokerRepositoryManager(SampleFulfiller(this))
    }

    @Suppress("UNCHECKED_CAST")
    inner class SampleFulfiller(context: Context): Fulfiller {

        private val map = HashMap<String, Any>()

        init {
            map.put("MEME", "Test transient only")
            map.put("TEST", 12345)
        }

        override fun <T> getLocal(key: String): T {
            Log.d("Fulfiller", "Requesting local: $key")
            Thread.sleep(1000L)
            return map[key] as T
        }

        override fun <T> getRemote(key: String): T {
            Log.d("Fulfiller", "Requesting remote: $key")
            Thread.sleep(4000L)
            return map[key] as T
        }

        override fun <T> putLocal(key: String, value: T) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun <T> putRemote(key: String, value: T) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun existsLocal(key: String): Boolean {
            return true
        }

    }

}