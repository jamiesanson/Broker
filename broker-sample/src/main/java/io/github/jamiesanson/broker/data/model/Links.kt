package io.github.jamiesanson.broker.data.model

import com.google.gson.annotations.SerializedName

data class Links(
    @SerializedName("mission_patch")
    val missionPatch: String
)
