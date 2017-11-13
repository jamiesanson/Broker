package io.github.jamiesanson.broker.annotation

import kotlin.annotation.AnnotationTarget.*

/**
 * Transient annotation for repository field.
 *
 * Usage:
 * `public Broker<List<Cats>> catsList;
 *
 * catsList.get( cats -> showCats(cats)) // Here cats are gathered through the remote fulfillment
 * // action. Get is an asynchronous action taking a callback
` *
 */
@Retention(AnnotationRetention.SOURCE)
@Target(FUNCTION)
annotation class Transient(
        val key: String
)
