package com.example.hck.data

import com.example.hck.data.requests.PlacesRequest
import com.example.hck.data.resp.PlacesResponse
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiCalls {

    @POST("/route/bestroute")
    suspend fun getRoute(
        @Body req: PlacesRequest
    ): PlacesResponse
}