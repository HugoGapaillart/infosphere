package com.infosphere.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infosphere.models.UnsplashPhoto
import com.infosphere.repository.UnsplashRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UnsplashViewModel(
    private val repository: UnsplashRepository = UnsplashRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _photo = MutableStateFlow<UnsplashPhoto?>(null)
    val photo: StateFlow<UnsplashPhoto?> = _photo.asStateFlow()

    fun loadRandomPhoto(query: String? = null, orientation: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val p = repository.getRandomPhoto(query = query, orientation = orientation)
                if (p != null) {
                    _photo.value = p
                } else {
                    _error.value = "Aucun résultat"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur réseau"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clear() {
        _photo.value = null
        _error.value = null
    }
}

