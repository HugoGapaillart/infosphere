package com.infosphere.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.infosphere.models.User
import com.infosphere.repository.AuthRepository
import com.infosphere.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = authRepository.getCurrentUser()
        _currentUser.value = user
        _authState.value = if (user != null) AuthState.Authenticated else AuthState.Unauthenticated
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signIn(email, password)
            
            result.onSuccess { user ->
                _currentUser.value = user
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
                // Create user document in Firestore
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
                _authState.value = AuthState.Authenticated
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Unknown error")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
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
}
