package io.github.jamiesanson.broker.di

import dagger.Module
import dagger.Provides
import io.github.jamiesanson.broker.BrokerSampleApp
import javax.inject.Singleton

@Module
class AppModule(private val app: BrokerSampleApp) {

    @Provides @Singleton
    fun provideApplication(): BrokerSampleApp = app

}