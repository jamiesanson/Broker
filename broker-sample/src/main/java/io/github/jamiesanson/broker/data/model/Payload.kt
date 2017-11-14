package io.github.jamiesanson.broker.data.model

import com.google.gson.annotations.SerializedName

data class Payload(
        @SerializedName("payload_id")
        val id: String,
        val customers: List<String>,
        @SerializedName("payload_type")
        val type: String,
        @SerializedName("payload_mass_kg")
        val mass: Double,
        val orbit: String
)