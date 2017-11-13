package io.github.jamiesanson.broker.repo

import io.github.jamiesanson.broker.annotation.BrokerRepo
import io.github.jamiesanson.broker.annotation.Persistent
import io.github.jamiesanson.broker.annotation.Transient
import io.github.jamiesanson.broker.fulfillment.Broker
import io.github.jamiesanson.broker.fulfillment.PersistenceType
import io.github.jamiesanson.broker.util.ExpireDuration

@BrokerRepo
interface TestRepo {

    @Transient(key = "MEME")
    fun transientTestString(): Broker<String>

    @Persistent(
            key = "TEST",
            cacheExpireDuration = ExpireDuration.FIVE_MINUTES)
    fun persistentTestInt(): Broker<Int>
}
