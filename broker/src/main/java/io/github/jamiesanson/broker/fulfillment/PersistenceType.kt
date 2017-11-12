package io.github.jamiesanson.broker.fulfillment

/**
 * Enumerated class describing type of persistence the field is annotated as
 */
enum class PersistenceType {
    TRANSIENT,
    PERSISTENT,
    PERSISTENT_LOCAL_ONLY
}