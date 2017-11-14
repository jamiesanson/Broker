package io.github.jamiesanson.broker.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Launch(
        @SerializedName("flight_number")
        val flightNumber: Int,
        @SerializedName("launch_year")
        val launchYear: String,
        @SerializedName("launch_date_utc")
        val launchDateUTC: Date,
        val rocket: Rocket,
        val details: String,
        val payloads: List<Payload>,
        @SerializedName("landing_type")
        val landingType: String,
        @SerializedName("landing_vehicle")
        val landingVehicle: String,
        val links: Links
)
