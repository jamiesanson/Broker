package io.github.jamiesanson.broker.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.github.jamiesanson.broker.BrokerSampleApp
import io.github.jamiesanson.broker.data.network.SpaceXService
import io.paperdb.Book
import io.paperdb.Paper
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class DataModule(app: BrokerSampleApp) {

    init {
        Paper.init(app)
    }

    @Provides @Singleton
    fun provideRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://api.spacexdata.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }

    @Provides @Singleton
    fun provideGson(): Gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'").create()

    @Provides @Singleton
    fun provideSpaceXService(retrofit: Retrofit): SpaceXService =
            retrofit.create(SpaceXService::class.java)

    @Provides @Singleton
    fun provideBook(): Book = Paper.book()
}