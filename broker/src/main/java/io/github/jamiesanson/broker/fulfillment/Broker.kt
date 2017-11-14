package io.github.jamiesanson.broker.fulfillment

import io.github.jamiesanson.broker.event.SyncEvent
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.joda.time.Duration
import org.joda.time.LocalDateTime

/**
 * Class for wrapping repository return types. Should not be
 * instantiated directly.
 */
class Broker<T>(
        override val key: String,
        override val persistenceType: PersistenceType,
        override var lastFetched: LocalDateTime,
        override var lastUpdated: LocalDateTime,
        override val expirationDuration: Duration,
        private  val managerProvider: Provider<FulfillmentManager>
): DataInfo {

    init {
        EventBus.getDefault().register(this)

        // TODO Retrieve fetched and updated values from cache asynchronously

    }

    /**
     * Function to getting the value backing this Broker.
     *
     * @return Single to be subscribed to
     */
    fun get(): Single<T> {
        val manager = managerProvider.get()
        return manager
                .get<T>(this)
                .map {
                    val (value, dataInfo) = it
                    updateInfo(dataInfo)
                    return@map value
                }
    }

    /**
     * Function to update the state of the data
     *
     * @return Completable to be subscribed to
     */
    fun put(value: T): Completable {
        val manager = managerProvider.get()
        return manager.put(this, value)
                .flatMapCompletable {
                    updateInfo(info = it)
                    return@flatMapCompletable Completable.complete()
                }
    }

    /**
     * Invalidates this Broker such that the next call to get will
     * bypass local persistence if it exists.
     */
    fun invalidate() {
        lastFetched = lastFetched.minus(expirationDuration)
    }

    /**
     * Updates data info from state emitted by fulfillment manager
     */
    private fun updateInfo(info: DataInfo) {
        lastFetched = info.lastFetched
        lastUpdated = info.lastUpdated
    }

    /**
     * Called by the repository to deliberately sync local
     * content to the remote if it exists
     */
    @Suppress("unused")
    @Subscribe
    fun sync(event: SyncEvent) {
        if (event.syncType == SyncEvent.Type.IMMEDIATE) {
            get()
                .flatMapCompletable {
                    put(it)
                }
                .subscribeOn(Schedulers.io())
                .subscribe()
        }
    }

    /**
     * Class for storing information in a disk backed LRU cache
     * relating to this broker
     */
    data class CacheValues(
            val lastFetched: LocalDateTime,
            var lastUpdated: LocalDateTime
    ) {
        companion object {
            val lastFetchedIndex = 0
            val lastUpdatedIndex = 1
        }
    }
}