package com.example.hck.data.resp.map


import com.google.gson.annotations.SerializedName

data class Maneuver(
    @SerializedName("bearing_after")
    val bearingAfter: Int = 0,
    @SerializedName("bearing_before")
    val bearingBefore: Int = 0,
    @SerializedName("instruction")
    val instruction: String = "",
    @SerializedName("location")
    val location: List<Double> = listOf(),
    @SerializedName("modifier")
    val modifier: String? = null,
    @SerializedName("type")
    val type: String = ""
)