package io.github.jamiesanson.broker.fulfillment

/**
 * Enumerated class describing state of data held in Broker
 */
enum class DataState {
    LOCAL_FRESH,
    LOCAL_STALE,
    REMOTE
}
