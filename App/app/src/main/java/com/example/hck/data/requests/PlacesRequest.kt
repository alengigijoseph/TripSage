package com.example.hck.data.requests

data class PlacesRequest(

    val places: List<Rd>
)

data class Rd(
    val roadnum: Int,
    val rdnames: List<String>
)