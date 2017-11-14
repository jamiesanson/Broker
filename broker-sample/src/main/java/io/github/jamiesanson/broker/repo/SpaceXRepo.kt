package io.github.jamiesanson.broker.repo

import io.github.jamiesanson.broker.annotation.BrokerRepo
import io.github.jamiesanson.broker.annotation.Persistent
import io.github.jamiesanson.broker.annotation.Transient
import io.github.jamiesanson.broker.data.model.Launch
import io.github.jamiesanson.broker.fulfillment.Broker
import io.github.jamiesanson.broker.util.ExpireDuration

@BrokerRepo
interface SpaceXRepo {

    @Transient(key = "latest")
    fun latestLaunch(): Broker<Launch>

    @Persistent(
            key = "upcoming",
            cacheExpireDuration = ExpireDuration.FIVE_MINUTES)
    fun upcomingLaunches(): Broker<List<Launch>>

    @Persistent(key = "past")
    fun pastLaunches(): Broker<List<Launch>>
}
