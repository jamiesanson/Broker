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
    }

    /**
     * Function for getting the value backing this Broker.
     *
     * @param callback Callback for async operation
     */
    fun get(callback: Callback<T>) {
        get()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {item -> callback.onRetrieved(item)},
                        {error -> callback.onError(error)}
                )
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
     * @param value Value to be put into the broker
     * @param callback callback called on completion
     */
    fun put(value: T, putCallback: PutCallback) {
        put(value)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { putCallback.onComplete() },
                        { putCallback.onError(it)  }
                )
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
    }

    /**
     * Called by the repository to deliberately sync local
     * content to the remote if it exists
     */
    @Suppress("unused")
    @Subscribe
    fun sync(event: SyncEvent) {
        if (event.syncType == SyncEvent.Type.IMMEDIATE) {
            lastUpdated
            get()
                .flatMapCompletable {
                    put(it)
                }
                .subscribeOn(Schedulers.io())
                .subscribe()
        }
    }

    /**
     * Callback used for get operations
     */
    inner class Callback<in T>(
            val onRetrieved: (item: T) -> Unit,
            val onError: (throwable: Throwable) -> Unit = {})

    /**
     * Callback used for put operations
     */
    inner class PutCallback(
            val onComplete: () -> Unit,
            val onError: (throwable: Throwable) -> Unit = {}
    )
}