package io.github.jamiesanson.broker.repo

import android.util.Log
import io.github.jamiesanson.broker.data.network.SpaceXService
import io.github.jamiesanson.broker.fulfillment.Fulfiller
import io.paperdb.Book

/**
 * Implementation of a fulfiller for demonstrating Broker
 */
class SampleFulfiller(
        private val spaceXService: SpaceXService,
        private val paperBook: Book
): Fulfiller {
    override fun <T> getLocal(key: String): T {
        Log.d("SampleFulfiller", "getLocal: $key")
        return paperBook.read(key)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getRemote(key: String): T {
        Log.d("SampleFulfiller", "getRemote: $key")
        return when (key) {
            "latest" -> spaceXService.latestLaunch().blockingGet() as T
            "upcoming" -> spaceXService.upcomingLaunches().blockingGet() as T
            "past" -> spaceXService.pastLaunches().blockingGet() as T
            else -> throw IllegalStateException("Key $key not handled in getRemote")
        }
    }

    override fun <T> putLocal(key: String, value: T) {
        Log.d("SampleFulfiller", "putLocal: $key")
        paperBook.write(key, value)
    }

    override fun <T> putRemote(key: String, value: T) {
        throw IllegalStateException("Put remote not supported")
    }

    override fun existsLocal(key: String): Boolean {
        Log.d("SampleFulfiller", "existsLocal: $key")
        return paperBook.contains(key)
    }

}