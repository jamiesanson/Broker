package io.github.jamiesanson.broker.fulfillment

import io.github.jamiesanson.broker.util.isContentStagnant
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.SingleSource
import org.joda.time.LocalDateTime

/**
 * Class for managing fulfillment. Should only be directly used through
 * the {@link Broker} class.
 */
class FulfillmentManager(
        private val fulfiller: Fulfiller
) {

    /**
     * Retrieves value associated with the {@link DataInfo}
     * instance.
     *
     * @param info DataInfo to retrieve
     * @return Returns a Single of the requested value paired to the new DataInfo
     */
    fun <T> get(info: DataInfo) : Single<Pair<T, DataInfo>> {
        return when (info.persistenceType) {
            PersistenceType.TRANSIENT -> getTransient(info)
            PersistenceType.PERSISTENT -> getPersistent(info)
            PersistenceType.PERSISTENT_LOCAL_ONLY -> getPersistent(info, skipRemote = true)
        }
    }

    /**
     * Updates value associated with the {@link DataInfo}
     * instance.
     *
     * @param info DataInfo to retrieve
     * @param value Value to update
     * @return Returns a Single of the new DataInfo when complete
     */
    fun <T> put(info: DataInfo, value: T): Single<DataInfo> {
        return when (info.persistenceType) {
            PersistenceType.TRANSIENT -> putTransient(info, value)
            PersistenceType.PERSISTENT -> putPersistent(info, value)
            PersistenceType.PERSISTENT_LOCAL_ONLY -> putPersistent(info, value)
        }
    }

    /**
     * Helper function for adapting get calls
     */
    private fun <T> adaptGet(info: DataInfo,
                             callable: (key: String) -> T,
                             updateFetched: Boolean = false): Single<Pair<T, DataInfo>> {

        return Single
                .create( SingleOnSubscribe<T> { emitter ->
                    val result = callable(info.key)
                    emitter.onSuccess(result)
                })
                .map {
                    if (updateFetched) {
                        info.lastFetched = LocalDateTime.now()
                    }
                    return@map it to info
                }
    }

    /**
     * Helper function for adapting put calls
     */
    private fun <T> adaptPut(info: DataInfo,
                             callable: (String, T) -> Unit,
                             value: T,
                             updatePutTime: Boolean = false): Single<DataInfo> {
        return Completable.create {
                    callable(info.key, value)
                    it.onComplete()
                }
                .andThen( SingleSource {
                    if (updatePutTime) {
                        info.lastUpdated = LocalDateTime.now()
                    }

                    it.onSuccess(info)
                })
    }

    private fun <T> getRemoteAndUpdateLocal(key: String): T {
        val item: T = fulfiller.getRemote(key)
        fulfiller.putLocal(key, item)
        return item
    }

    private fun <T> getTransient(info: DataInfo): Single<Pair<T, DataInfo>> {
        return adaptGet(info, fulfiller::getRemote, true)
    }

    private fun <T> getPersistent(dataInfo: DataInfo, skipRemote: Boolean = false): Single<Pair<T, DataInfo>> {
        // If the remote is to be skipped, let the user decide what they want to happen
        if (skipRemote) {
            return adaptGet(dataInfo, fulfiller::getLocal)
        }

        var updateFetched = false
        val returnValue: (String) -> T

        // Interesting bug in the analyser. It's fine with this form of assignment, but when directly
        // assigning type-coercion fails for some reason
        @Suppress("LiftReturnOrAssignment")
        if (fulfiller.existsLocal(dataInfo.key)) {
            // Check freshness of cached data.
            returnValue = if (dataInfo.isContentStagnant()) {
                updateFetched = true
                this::getRemoteAndUpdateLocal
            } else {
                fulfiller::getLocal
            }
        } else {
            updateFetched = true
            returnValue = this::getRemoteAndUpdateLocal
        }

        return adaptGet(dataInfo, returnValue, updateFetched)
    }

    private fun <T> putTransient(dataInfo: DataInfo, value: T): Single<DataInfo> {
        return adaptPut(dataInfo, fulfiller::putRemote, value, updatePutTime = true)
    }

    private fun <T> putPersistent(dataInfo: DataInfo, value: T): Single<DataInfo> {
        return if (dataInfo.persistenceType == PersistenceType.PERSISTENT_LOCAL_ONLY) {
            adaptPut(dataInfo, fulfiller::putLocal, value)
        } else {
            // Push the data to the remote, followed by updating the local version. The data info
            // emitted is that of the remote push. If the push fails, the info is not updated and
            // hence the old info without update time changes is emitted.
            adaptPut(dataInfo, fulfiller::putRemote, value, true)
                    .concatWith(
                            adaptPut(dataInfo, fulfiller::putLocal, value)
                    )
                    .first(dataInfo)
        }
    }
}
