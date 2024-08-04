package com.example.hck.data.resp.map


import com.google.gson.annotations.SerializedName

data class Routes(
    @SerializedName("routes")
    val routes: List<Route> = listOf()
)

data class Route(
    @SerializedName("distance")
    val distance: Double = 0.0,
    @SerializedName("duration")
    val duration: Double = 0.0,
    @SerializedName("legs")
    val legs: List<Leg> = listOf(),
)

data class Leg(
    @SerializedName("distance")
    val distance: Double = 0.0,
    @SerializedName("duration")
    val duration: Double = 0.0,
    @SerializedName("steps")
    val steps: List<Step> = listOf(),
    @SerializedName("summary")
    val summary: String = "",
)