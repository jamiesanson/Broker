package io.github.jamiesanson.broker.repo

import io.github.jamiesanson.broker.annotation.BrokerRepo
import io.github.jamiesanson.broker.fulfillment.Broker

@BrokerRepo
class TestRepo {

    @Transient
    lateinit var meme: Broker<String>
}
