package io.github.jamiesanson.broker.event

/**
 * Event class prompting a sync of all brokers.
 */
class SyncEvent(val syncType: Type) {

    enum class Type {
        IMMEDIATE
    }
}