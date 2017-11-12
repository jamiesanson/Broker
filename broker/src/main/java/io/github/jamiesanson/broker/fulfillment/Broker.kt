package io.github.jamiesanson.broker.fulfillment

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.joda.time.Duration
import org.joda.time.LocalDateTime

/**
 * Class for wrapping repository return types.
 */
class Broker<T>(
        override val key: String,
        override val persistenceType: PersistenceType,
        override var lastFetched: LocalDateTime,
        override var lastUpdated: LocalDateTime,
        override val expirationDuration: Duration,
        private val manager: FulfillmentManager
): DataInfo {

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
        return manager
                .get<T>(this)
                .map {
                    with(it) {
                        updateInfo(second)
                        return@map first
                    }
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
        return manager.put(this, value)
                .flatMapCompletable {
                    updateInfo(info = it)
                    return@flatMapCompletable Completable.complete()
                }
    }

    /**
     * Invalidates this Broker such that the next call to get will
     * bypass local persistence if it exists
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