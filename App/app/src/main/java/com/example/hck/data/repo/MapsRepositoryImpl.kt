package com.example.hck.data.repo

import com.example.hck.data.ApiCalls
import com.example.hck.data.requests.PlacesRequest
import com.example.hck.data.resp.PlacesResponse
import com.example.hck.domain.repo.MapsRepository

class MapsRepositoryImpl(
    private val api: ApiCalls
):MapsRepository {
    override suspend fun bestRoute(roads: PlacesRequest): PlacesResponse {
        return api.getRoute(roads)
    }
}