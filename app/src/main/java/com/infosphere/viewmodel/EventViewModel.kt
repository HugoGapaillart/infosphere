package com.infosphere.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.infosphere.models.Event
import com.infosphere.repository.AuthRepository
import com.infosphere.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val authRepository = AuthRepository()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _userEvents = MutableStateFlow<List<Event>>(emptyList())
    val userEvents: StateFlow<List<Event>> = _userEvents.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Event>>(emptyList())
    val searchResults: StateFlow<List<Event>> = _searchResults.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    fun loadEventsByCities(cityIds: List<String>) {
        viewModelScope.launch {
            eventRepository.getUpcomingEventsByCities(cityIds)
                .catch { e ->
                    _operationState.value = OperationState.Error(e.message ?: "Unknown error")
                }
                .collect { eventList ->
                    _events.value = eventList
                }
        }
    }

    fun loadUserEvents() {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        
        viewModelScope.launch {
            eventRepository.getEventsByUser(userId)
                .catch { e ->
                    _operationState.value = OperationState.Error(e.message ?: "Unknown error")
                }
                .collect { eventList ->
                    _userEvents.value = eventList
                }
        }
    }

    fun searchEvents(cityId: String? = null, eventTypes: List<String>? = null) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = eventRepository.searchEvents(cityId, eventTypes)
            
            result.onSuccess { eventList ->
                _searchResults.value = eventList
                _operationState.value = OperationState.Success
            }.onFailure { exception ->
                _operationState.value = OperationState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun createEvent(
        title: String,
        description: String,
        location: String,
        date: Timestamp,
        cityId: String,
        cityName: String,
        eventTypes: List<String>,
        photoUris: List<Uri>
    ) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _operationState.value = OperationState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            // Create event first
            val event = Event(
                title = title,
                description = description,
                location = location,
                date = date,
                photoUrls = emptyList(), // Will be updated after upload
                cityId = cityId,
                cityName = cityName,
                eventTypes = eventTypes,
                createdBy = userId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val createResult = eventRepository.createEvent(event)
            
            createResult.onSuccess { eventId ->
                // Upload photos
                val photoUrls = mutableListOf<String>()
                for (uri in photoUris) {
                    val uploadResult = eventRepository.uploadEventPhoto(eventId, uri)
                    uploadResult.onSuccess { url ->
                        photoUrls.add(url)
                    }.onFailure { exception ->
                        _operationState.value = OperationState.Error(
                            "Failed to upload photo: ${exception.message}"
                        )
                        return@launch
                    }
                }

                // Update event with photo URLs
                if (photoUrls.isNotEmpty()) {
                    eventRepository.updateEvent(eventId, mapOf("photoUrls" to photoUrls))
                }

                _operationState.value = OperationState.Success
            }.onFailure { exception ->
                _operationState.value = OperationState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = eventRepository.deleteEvent(eventId)
            
            result.onSuccess {
                _operationState.value = OperationState.Success
            }.onFailure { exception ->
                _operationState.value = OperationState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}
