package com.example.hck.domain.repo

import com.example.hck.data.requests.PlacesRequest
import com.example.hck.data.resp.PlacesResponse

interface MapsRepository {
    suspend fun bestRoute(roads: PlacesRequest): PlacesResponse
}