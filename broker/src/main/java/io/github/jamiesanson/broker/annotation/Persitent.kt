package io.github.jamiesanson.broker.annotation

import org.joda.time.Duration
import io.github.jamiesanson.broker.fulfillment.PersistenceType
import kotlin.annotation.AnnotationTarget.*

/**
 * Persistent annotation for repository field.
 *
 * Usage:
 * `public Broker<List<Cats>> catsList;
 *
 * catsList.get( cats -> showCats(cats)) // Here cats are gathered through the local fulfillment
 * // if it exists there, else it fetches from the remote
 * // if either it doesn't exist or the cache is invalidated
` *
 */
@Retention(AnnotationRetention.SOURCE)
@Target(FUNCTION)
annotation class Persistent(
        val key: String,
        val type: PersistenceType = PersistenceType.PERSISTENT,
        val cacheExpireDuration: ExpireDuration = ExpireDuration.FOUR_HOURS)

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
