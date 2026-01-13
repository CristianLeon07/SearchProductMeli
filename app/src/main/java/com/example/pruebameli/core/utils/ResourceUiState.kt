package com.example.pruebameli.core.utils

sealed class ResourceUiState<out T> {
    object Idle : ResourceUiState<Nothing>()
    object Loading : ResourceUiState<Nothing>()
    data class Success<out T>(val data: T) : ResourceUiState<T>()
    data class Error(val message: String) : ResourceUiState<Nothing>()

}