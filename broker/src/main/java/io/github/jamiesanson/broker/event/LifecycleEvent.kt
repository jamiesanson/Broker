package io.github.jamiesanson.broker.event

/**
 * Event class sent on different lifecycle events
 */
class LifecycleEvent(val eventType: Type) {

    enum class Type {
        ON_CREATE, ON_DESTROY
    }
}