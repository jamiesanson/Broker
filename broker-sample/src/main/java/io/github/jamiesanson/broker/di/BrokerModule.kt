package io.github.jamiesanson.broker.di

import dagger.Module
import dagger.Provides
import io.github.jamiesanson.broker.BrokerRepositoryManager
import io.github.jamiesanson.broker.BrokerSampleApp
import io.github.jamiesanson.broker.data.network.SpaceXService
import io.github.jamiesanson.broker.fulfillment.Fulfiller
import io.github.jamiesanson.broker.repo.SampleFulfiller
import io.github.jamiesanson.broker.repo.SpaceXRepo
import io.paperdb.Book
import javax.inject.Singleton

@Module
class BrokerModule(private val app: BrokerSampleApp) {

    @Provides @Singleton
    fun provideTestRepo(manager: BrokerRepositoryManager): SpaceXRepo =
            manager.getRepo(SpaceXRepo::class.java)

    @Provides @Singleton
    fun provideRepositoryManager(fulfiller: Fulfiller): BrokerRepositoryManager =
            BrokerRepositoryManager(fulfiller)

    @Provides @Singleton
    fun provideFulfiller(spaceXService: SpaceXService, paperBook: Book): Fulfiller =
            SampleFulfiller(spaceXService, paperBook)
}