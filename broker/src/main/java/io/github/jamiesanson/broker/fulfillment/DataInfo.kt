package io.github.jamiesanson.broker.fulfillment

import org.joda.time.Duration
import org.joda.time.LocalDateTime

/**
 * Interface outlining important information about the state of the data
 */
interface DataInfo {
    // The Key related to the data
    val key: String

    // The last time this data was fetched from the remote
    var lastFetched: LocalDateTime

    // The last time this data was updated, i.e pushed to the remote.
    // May not be applicable to some types
    var lastUpdated: LocalDateTime

    // How long the local cached data is relevant for
    val expirationDuration: Duration

    // The type of persistence this data is held in
    val persistenceType: PersistenceType
}