package io.github.jamiesanson.broker

import android.app.Application
import io.github.jamiesanson.broker.di.AppComponent
import io.github.jamiesanson.broker.di.BrokerModule
import io.github.jamiesanson.broker.di.DaggerAppComponent
import io.github.jamiesanson.broker.di.DataModule

class BrokerSampleApp: Application() {

    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder()
                .brokerModule(BrokerModule(this))
                .dataModule(DataModule(this))
                .build()
    }


}