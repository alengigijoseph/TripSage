package com.example.hck.data.resp

import com.google.gson.annotations.SerializedName
import java.io.Serial

data class PlacesResponse(
    @SerializedName("data")
    val data: List<Dataa>
)

data class Dataa(
    @SerializedName("route")
    val roadnum: Int,
    @SerializedName("roads")
    val roads: List<Roads>

)

data class Roads(
    @SerializedName("roadName")
    val name: String,
    @SerializedName("time")
    val time: Double = 40.00,
    @SerializedName("construction")
    val constrctn: List<Coords>
)

data class Coords(
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lng")
    val lng: String
)