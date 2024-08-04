package com.example.hck.presentation.ui.map

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedStateRegistryOwner
import com.example.hck.data.requests.PlacesRequest
import com.example.hck.data.resp.PlacesResponse
import com.example.hck.domain.repo.MapsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


class MapViewModel(
    val repo: MapsRepository
): ViewModel(){


    suspend fun bestRoute(roads: PlacesRequest): PlacesResponse {
        return repo.bestRoute(roads)
    }

    companion object {
        fun provideFactory(
            myRepository: MapsRepository,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return MapViewModel(myRepository) as T
                }
            }
    }


}