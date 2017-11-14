package io.github.jamiesanson.broker.di

import dagger.Component
import io.github.jamiesanson.broker.BrokerSampleApp
import io.github.jamiesanson.broker.MainActivity
import javax.inject.Singleton

@Singleton
@Component(
        modules = arrayOf(
                AppModule::class,
                BrokerModule::class,
                DataModule::class
        )
)
interface AppComponent {

    fun inject(app: BrokerSampleApp)

    fun inject(activity: MainActivity)
}