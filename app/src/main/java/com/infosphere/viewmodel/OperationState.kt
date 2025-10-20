package com.infosphere.viewmodel

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    object Success : OperationState()
    data class Error(val message: String) : OperationState()
}
