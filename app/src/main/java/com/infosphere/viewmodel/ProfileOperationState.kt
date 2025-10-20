package com.infosphere.viewmodel

sealed class ProfileOperationState {
    object Idle : ProfileOperationState()
    object Loading : ProfileOperationState()
    object Success : ProfileOperationState()
    data class Error(val message: String) : ProfileOperationState()
}
