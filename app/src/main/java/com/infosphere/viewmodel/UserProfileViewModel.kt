package com.infosphere.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infosphere.models.City
import com.infosphere.models.EventType
import com.infosphere.models.User
import com.infosphere.repository.AuthRepository
import com.infosphere.repository.CityRepository
import com.infosphere.repository.EventTypeRepository
import com.infosphere.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val cityRepository = CityRepository()
    private val eventTypeRepository = EventTypeRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _allCities = MutableStateFlow<List<City>>(emptyList())
    val allCities: StateFlow<List<City>> = _allCities.asStateFlow()

    private val _allEventTypes = MutableStateFlow<List<EventType>>(emptyList())
    val allEventTypes: StateFlow<List<EventType>> = _allEventTypes.asStateFlow()

    private val _operationState = MutableStateFlow<ProfileOperationState>(ProfileOperationState.Idle)
    val operationState: StateFlow<ProfileOperationState> = _operationState.asStateFlow()

    init {
        loadUserProfile()
        loadAllCities()
        loadAllEventTypes()
    }

    fun loadUserProfile() {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            userRepository.getUserFlow(userId)
                .catch { e ->
                    _operationState.value = ProfileOperationState.Error(e.message ?: "Unknown error")
                }
                .collect { userData ->
                    _user.value = userData
                }
        }
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

    fun loadAllEventTypes() {
        viewModelScope.launch {
            val result = eventTypeRepository.getAllEventTypes()
            result.onSuccess { types ->
                _allEventTypes.value = types
            }.onFailure { exception ->
                _operationState.value = ProfileOperationState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun updateUserCities(cityIds: List<String>) {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            _operationState.value = ProfileOperationState.Loading
            val result = userRepository.updateUserCities(userId, cityIds)

            result.onSuccess {
                _operationState.value = ProfileOperationState.Success
            }.onFailure { exception ->
                _operationState.value = ProfileOperationState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = ProfileOperationState.Idle
    }
}
