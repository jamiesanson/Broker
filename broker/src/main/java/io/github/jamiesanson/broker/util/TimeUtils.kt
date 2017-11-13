package io.github.jamiesanson.broker.util

import io.github.jamiesanson.broker.fulfillment.DataInfo
import org.joda.time.Duration
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

enum class ExpireDuration {
    FIVE_MINUTES, ONE_HOUR, TWO_HOURS, FOUR_HOURS, EIGHT_HOURS, ONE_DAY;

    fun toDuration(): Duration {
        return when (this) {
            FIVE_MINUTES -> Duration.standardMinutes(5)
            ONE_HOUR -> Duration.standardHours(1)
            TWO_HOURS -> Duration.standardHours(2)
            FOUR_HOURS -> Duration.standardHours(4)
            EIGHT_HOURS -> Duration.standardHours(8)
            ONE_DAY -> Duration.standardDays(1)
        }
    }
}