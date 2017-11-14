package io.github.jamiesanson.broker.annotation

import io.github.jamiesanson.broker.fulfillment.PersistenceType
import io.github.jamiesanson.broker.util.ExpireDuration
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
