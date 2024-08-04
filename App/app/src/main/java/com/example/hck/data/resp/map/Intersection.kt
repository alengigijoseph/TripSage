package com.example.hck.data.resp.map


import com.google.gson.annotations.SerializedName

data class Intersection(
    @SerializedName("admin_index")
    val adminIndex: Int = 0,
    @SerializedName("bearings")
    val bearings: List<Int> = listOf(),
    @SerializedName("duration")
    val duration: Double? = null,
    @SerializedName("entry")
    val entry: List<Boolean> = listOf(),
    @SerializedName("geometry_index")
    val geometryIndex: Int = 0,
    @SerializedName("in")
    val inX: Int? = null,
    @SerializedName("is_urban")
    val isUrban: Boolean? = null,
    @SerializedName("location")
    val location: List<Double> = listOf(),
    @SerializedName("out")
    val `out`: Int? = null,
    @SerializedName("turn_duration")
    val turnDuration: Double? = null,
    @SerializedName("turn_weight")
    val turnWeight: Double? = null,
    @SerializedName("weight")
    val weight: Double? = null
)