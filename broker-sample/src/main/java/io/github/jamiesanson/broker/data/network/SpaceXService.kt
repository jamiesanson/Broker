package io.github.jamiesanson.broker.data.network

import io.github.jamiesanson.broker.data.model.Launch
import io.reactivex.Single
import retrofit2.http.GET

/**
 * Retrofit interface for SpaceX API
 *
 * More info here: https://github.com/r-spacex/SpaceX-API
 */
interface SpaceXService {

    @GET("v1/launches/latest")
    fun latestLaunch(): Single<Launch>

    @GET("v1/launches")
    fun pastLaunches(): Single<List<Launch>>

    @GET("v1/launches/upcoming")
    fun upcomingLaunches(): Single<List<Launch>>

}