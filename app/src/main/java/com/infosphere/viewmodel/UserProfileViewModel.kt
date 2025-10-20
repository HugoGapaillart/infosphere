package com.infosphere.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infosphere.models.City
import com.infosphere.models.EventType
import com.infosphere.models.User
import com.infosphere.repository.AuthRepository
import com.infosphere.repository.CityRepository
import com.infosphere.repository.EventTypeRepository
import com.infosphere.repository.UserRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val cityRepository = CityRepository()
    private val eventTypeRepository = EventTypeRepository()
    private val authRepository = AuthRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _allCities = MutableLiveData<List<City>>()
    val allCities: LiveData<List<City>> = _allCities

    private val _allEventTypes = MutableLiveData<List<EventType>>()
    val allEventTypes: LiveData<List<EventType>> = _allEventTypes

    private val _updateState = MutableLiveData<UpdateState>()
    val updateState: LiveData<UpdateState> = _updateState

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
                    _updateState.value = UpdateState.Error(e.message ?: "Unknown error")
                }
                .collect { userData ->
                    _user.value = userData
                }
        }
    }

    fun loadAllCities() {
        viewModelScope.launch {
            val result = cityRepository.getAllCities()
            result.onSuccess {
                _allCities.value = it
            }.onFailure { 
                _updateState.value = UpdateState.Error(it.message ?: "Unknown error")
            }
        }
    }

    fun loadAllEventTypes() {
        viewModelScope.launch {
            val result = eventTypeRepository.getAllEventTypes()
            result.onSuccess {
                _allEventTypes.value = it
            }.onFailure { 
                _updateState.value = UpdateState.Error(it.message ?: "Unknown error")
            }
        }
    }

    fun updateUserCities(cityIds: List<String>) {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            val result = userRepository.updateUserCities(userId, cityIds)
            
            result.onSuccess {
                _updateState.value = UpdateState.Success
            }.onFailure { 
                _updateState.value = UpdateState.Error(it.message ?: "Unknown error")
            }
        }
    }
    
    fun searchCities(query: String) {
        viewModelScope.launch {
            val result = cityRepository.searchCities(query)
            result.onSuccess {
                _allCities.value = it
            }.onFailure { 
                _updateState.value = UpdateState.Error(it.message ?: "Unknown error")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}
