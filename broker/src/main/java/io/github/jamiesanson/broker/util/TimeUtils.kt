package io.github.jamiesanson.broker.util

import io.github.jamiesanson.broker.fulfillment.DataInfo
import org.joda.time.LocalDateTime

/**
 * Checks stagnation of data. Last fetched + expiration duration should be after
 * the current time for data to be considered fresh
 */
fun DataInfo.isContentStagnant(): Boolean {
    return this.lastFetched
            .plus(this.expirationDuration)
            .isBefore(LocalDateTime.now())
}