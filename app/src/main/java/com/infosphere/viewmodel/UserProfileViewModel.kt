package com.infosphere.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infosphere.models.City
import com.infosphere.repository.CityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val cityRepository = CityRepository()

    private val _allCities = MutableStateFlow<List<City>>(emptyList())
    val allCities: StateFlow<List<City>> = _allCities.asStateFlow()

    private val _operationState = MutableStateFlow<ProfileOperationState>(ProfileOperationState.Idle)
    val operationState: StateFlow<ProfileOperationState> = _operationState.asStateFlow()

    init {
        loadAllCities()
    }

    fun loadAllCities() {
        viewModelScope.launch {
            val result = cityRepository.getAllCities()
            result.onSuccess { cities ->
                _allCities.value = cities
            }.onFailure { exception ->
                _operationState.value = ProfileOperationState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = ProfileOperationState.Idle
    }
}

sealed class ProfileOperationState {
    object Idle : ProfileOperationState()
    object Loading : ProfileOperationState()
    object Success : ProfileOperationState()
    data class Error(val message: String) : ProfileOperationState()
}

