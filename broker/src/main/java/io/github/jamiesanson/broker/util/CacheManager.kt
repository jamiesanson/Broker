package io.github.jamiesanson.broker.util

import android.util.Base64
import com.jakewharton.disklrucache.DiskLruCache
import io.github.jamiesanson.broker.fulfillment.Broker
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.LocalDateTime
import java.io.*

/**
 * Manager class for interacting with a DiskLruCache
 */
class CacheManager {
    companion object {
        @JvmStatic
        val CACHE_FILE_NAME = "brokercache"
        @JvmStatic
        val CACHE_VERSION = 1
        @JvmStatic
        val CACHE_VALUE_COUNT = 2
        @JvmStatic
        val MAX_CACHE_SIZE = 1024*50L
    }

    private val diskCache: DiskLruCache

    init {
        diskCache = DiskLruCache.open(
                File(CACHE_FILE_NAME),
                CACHE_VERSION,
                CACHE_VALUE_COUNT,
                MAX_CACHE_SIZE
        )
    }

    fun retrieveBrokerCachedFields(key: String, callback: (values: Broker.CacheValues) -> Unit) {
        Single.create<Broker.CacheValues> {
            val snapshot = diskCache.get(key)
            val fetched: LocalDateTime = snapshot
                    .getString(Broker.CacheValues.lastFetchedIndex)
                    .deserialise()
            val updated: LocalDateTime = snapshot
                    .getString(Broker.CacheValues.lastUpdatedIndex)
                    .deserialise()

            it.onSuccess(
                    Broker.CacheValues(lastFetched = fetched, lastUpdated = updated)
            )
        }.subscribeOn(Schedulers.io())
                .subscribe({ cached ->
                    callback(cached)
                }, Throwable::printStackTrace)
    }

    // Serialization helper functions
    @Throws(IOException::class)
    private fun Serializable.toBase64String(): String {
        val byteOutStream = ByteArrayOutputStream()
        val outStream = ObjectOutputStream(byteOutStream)
        outStream.writeObject(this)
        outStream.close()
        return Base64.encodeToString(byteOutStream.toByteArray(), Base64.DEFAULT)
    }


    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class, ClassCastException::class)
    private fun <T> String.deserialise(): T {
        val data = Base64.decode(this, Base64.DEFAULT)
        val inputStream = ObjectInputStream(
                ByteArrayInputStream(data))
        val readObject = inputStream.readObject()
        inputStream.close()
        return readObject as T
    }
}