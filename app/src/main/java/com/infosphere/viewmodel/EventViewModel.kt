package com.infosphere.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.infosphere.models.Event
import com.infosphere.models.EventType
import com.infosphere.repository.AuthRepository
import com.infosphere.repository.EventRepository
import com.infosphere.repository.EventTypeRepository
import com.infosphere.repository.UnsplashRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val authRepository = AuthRepository()
    private val eventTypeRepository = EventTypeRepository()
    private val unsplashRepository = UnsplashRepository()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _userEvents = MutableStateFlow<List<Event>>(emptyList())
    val userEvents: StateFlow<List<Event>> = _userEvents.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Event>>(emptyList())
    val searchResults: StateFlow<List<Event>> = _searchResults.asStateFlow()

    private val _userEventTypes = MutableStateFlow<Map<String, EventType>>(emptyMap())
    val userEventTypes: StateFlow<Map<String, EventType>> = _userEventTypes.asStateFlow()

    private val _allEventTypes = MutableStateFlow<List<EventType>>(emptyList())
    val allEventTypes: StateFlow<List<EventType>> = _allEventTypes.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    init {
        loadAllEventTypes()
    }

    private fun loadAllEventTypes() {
        viewModelScope.launch {
            val result = eventTypeRepository.getAllEventTypes()
            result.onSuccess { types ->
                _allEventTypes.value = types
            }.onFailure { exception ->
                _operationState.value = OperationState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun loadEventsByCities(cityIds: List<String>) {
        viewModelScope.launch {
            eventRepository.getEventsByCities(cityIds)
                .catch { e ->
                    _operationState.value = OperationState.Error(e.message ?: "Erreur inconnue")
                }
                .collect { eventList ->
                    val now = com.google.firebase.Timestamp.now()
                    val upcomingEvents = eventList
                        .filter { event -> event.date.seconds > now.seconds }
                        .sortedBy { it.date.seconds }

                    _events.value = upcomingEvents
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

                    // Load associated EventTypes
                    val uniqueTypeIds = eventList.flatMap { it.eventTypes }.distinct()
                    if (uniqueTypeIds.isNotEmpty()) {
                        loadEventTypesForEvents(uniqueTypeIds)
                    }
                }
        }
    }

    private suspend fun loadEventTypesForEvents(typeIds: List<String>) {
        val eventTypesMap = mutableMapOf<String, EventType>()

        typeIds.forEach { typeId ->
            val result = eventTypeRepository.getEventTypeById(typeId)
            result.onSuccess { eventType ->
                if (eventType != null) {
                    eventTypesMap[typeId] = eventType
                }
            }
        }

        _userEventTypes.value = eventTypesMap
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
        date: Timestamp,
        cityId: String,
        cityName: String,
        typeIds: List<String>,
        photoUris: List<Uri>,
        unsplashPhotoUrl: String? = null
    ) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _operationState.value = OperationState.Error("Utilisateur non authentifié")
            return
        }

        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val event = Event(
                title = title,
                description = description,
                location = "", // Can be added later if needed
                date = date,
                photoUrls = emptyList(),
                cityId = cityId,
                cityName = cityName,
                eventTypes = typeIds,
                createdBy = userId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val createResult = eventRepository.createEvent(event)

            createResult.onSuccess { eventId ->
                val photoUrls = mutableListOf<String>()

                if (photoUris.isNotEmpty()) {
                    for (uri in photoUris) {
                        val uploadResult = eventRepository.uploadEventPhoto(eventId, uri)
                        uploadResult.onSuccess { url ->
                            photoUrls.add(url)
                        }.onFailure { exception ->
                            _operationState.value = OperationState.Error(
                                "Erreur lors de l'upload: ${exception.message}"
                            )
                            return@launch
                        }
                    }
                } else if (!unsplashPhotoUrl.isNullOrEmpty()) {
                    // Utiliser l'URL Unsplash prévisualisée
                    Log.d("EventViewModel", "Using previewed Unsplash image: $unsplashPhotoUrl")
                    photoUrls.add(unsplashPhotoUrl)
                } else {
                    // Charger une nouvelle image aléatoire seulement si aucune image n'est fournie
                    try {
                        val remote = unsplashRepository.getRandomPhoto()
                        val url = remote?.urls?.regular ?: remote?.urls?.small
                        if (!url.isNullOrEmpty()) {
                            Log.d("EventViewModel", "Fetched new random Unsplash image: $url")
                            photoUrls.add(url)
                        }
                    } catch (e: Exception) {
                        Log.w("EventViewModel", "Failed to fetch Unsplash image", e)
                    }
                }

                if (photoUrls.isNotEmpty()) {
                    eventRepository.updateEvent(eventId, mapOf("photoUrls" to photoUrls))
                }

                _operationState.value = OperationState.Success
                loadUserEvents()
            }.onFailure { exception ->
                _operationState.value = OperationState.Error(
                    exception.message ?: "Erreur lors de la création"
                )
            }
        }
    }

    suspend fun getEvent(eventId: String): Result<Event?> {
        return eventRepository.getEvent(eventId)
    }

    fun updateEvent(
        eventId: String,
        title: String,
        description: String,
        date: Timestamp,
        cityId: String,
        cityName: String,
        typeIds: List<String>,
        existingPhotoUrls: List<String>,
        newPhotoUris: List<Uri>,
        unsplashPhotoUrl: String? = null
    ) {
        val userId = authRepository.getCurrentUser()?.uid
        if (userId == null) {
            _operationState.value = OperationState.Error("Utilisateur non authentifié")
            return
        }

        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val photoUrls = existingPhotoUrls.toMutableList()

            // Upload new photos
            if (newPhotoUris.isNotEmpty()) {
                for (uri in newPhotoUris) {
                    val uploadResult = eventRepository.uploadEventPhoto(eventId, uri)
                    uploadResult.onSuccess { url ->
                        photoUrls.add(url)
                    }.onFailure { exception ->
                        _operationState.value = OperationState.Error(
                            "Erreur lors de l'upload: ${exception.message}"
                        )
                        return@launch
                    }
                }
            } else if (existingPhotoUrls.isEmpty() && !unsplashPhotoUrl.isNullOrEmpty()) {
                // Utiliser l'URL Unsplash prévisualisée
                Log.d("EventViewModel", "Using previewed Unsplash image: $unsplashPhotoUrl")
                photoUrls.add(unsplashPhotoUrl)
            } else if (existingPhotoUrls.isEmpty() && newPhotoUris.isEmpty()) {
                // Charger une nouvelle image aléatoire seulement si aucune image n'est fournie
                try {
                    val remote = unsplashRepository.getRandomPhoto()
                    val url = remote?.urls?.regular ?: remote?.urls?.small
                    if (!url.isNullOrEmpty()) {
                        Log.d("EventViewModel", "Fetched new random Unsplash image: $url")
                        photoUrls.add(url)
                    }
                } catch (e: Exception) {
                    Log.w("EventViewModel", "Failed to fetch Unsplash image", e)
                }
            }

            val updates = mapOf(
                "title" to title,
                "description" to description,
                "date" to date,
                "cityId" to cityId,
                "cityName" to cityName,
                "eventTypes" to typeIds,
                "photoUrls" to photoUrls
            )

            val updateResult = eventRepository.updateEvent(eventId, updates)

            updateResult.onSuccess {
                _operationState.value = OperationState.Success
                loadUserEvents()
            }.onFailure { exception ->
                _operationState.value = OperationState.Error(
                    exception.message ?: "Erreur lors de la mise à jour"
                )
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = eventRepository.deleteEvent(eventId)

            result.onSuccess {
                _operationState.value = OperationState.Success
                loadUserEvents()
            }.onFailure { exception ->
                _operationState.value = OperationState.Error(
                    exception.message ?: "Erreur lors de la suppression"
                )
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    object Success : OperationState()
    data class Error(val message: String) : OperationState()
}
