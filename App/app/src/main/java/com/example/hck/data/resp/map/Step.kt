package com.example.hck.data.resp.map


import com.google.gson.annotations.SerializedName

data class Step(
    @SerializedName("distance")
    val distance: Double = 0.0,
    @SerializedName("driving_side")
    val drivingSide: String = "",
    @SerializedName("duration")
    val duration: Double = 0.0,
    @SerializedName("geometry")
    val geometry: String = "",
    @SerializedName("intersections")
    val intersections: List<Intersection> = listOf(),
    @SerializedName("maneuver")
    val maneuver: Maneuver = Maneuver(),
    @SerializedName("mode")
    val mode: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("ref")
    val ref: String? = null,
    @SerializedName("weight")
    val weight: Double = 0.0
)