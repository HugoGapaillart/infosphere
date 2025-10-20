package com.infosphere.viewmodel

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object PasswordResetSent : AuthState()
    data class Error(val message: String) : AuthState()
}
