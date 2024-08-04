package com.example.hck.presentation.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val _from = MutableLiveData<String>().apply {
        value = "Current Location"
    }
    val from: LiveData<String> = _from

    private val _to = MutableLiveData<String>().apply {
        value = "Select Destination"
    }
    val to: LiveData<String> = _to

    fun updateFromValue(newValue: String) {
        _from.value = newValue
    }

    fun updateToValue(newValue: String) {
        _to.value = newValue
    }
}