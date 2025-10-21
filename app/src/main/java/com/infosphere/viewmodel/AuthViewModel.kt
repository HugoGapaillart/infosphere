package com.infosphere.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.infosphere.models.User
import com.infosphere.repository.AuthRepository
import com.infosphere.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = authRepository.getCurrentUser()
        _currentUser.value = user
        _authState.value = if (user != null) {
            loadUserProfile()
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }

    private fun loadUserProfile() {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            userRepository.getUserFlow(userId)
                .collect { userData ->
                    _userProfile.value = userData
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signIn(email, password)
            
            result.onSuccess { user ->
                _currentUser.value = user
                loadUserProfile()
                _authState.value = AuthState.Authenticated
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUp(email, password, displayName)
            
            result.onSuccess { firebaseUser ->
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    selectedCityIds = emptyList(),
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
                
                userRepository.createUser(user)
                _currentUser.value = firebaseUser
                loadUserProfile()
                _authState.value = AuthState.Authenticated
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _userProfile.value = null
        _authState.value = AuthState.Unauthenticated
    }

    fun updateUserCities(cityIds: List<String>) {
        val userId = authRepository.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            val result = userRepository.updateUserCities(userId, cityIds)
            result.onSuccess {
                // Profile will be updated automatically via Flow
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.resetPassword(email)
            
            result.onSuccess {
                _authState.value = AuthState.PasswordResetSent
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Unknown error")
            }
        }
    }
    
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = if (_currentUser.value != null) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
        }
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object PasswordResetSent : AuthState()
    data class Error(val message: String) : AuthState()
}
