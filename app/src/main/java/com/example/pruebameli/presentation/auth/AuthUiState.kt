package com.example.pruebameli.presentation.auth


sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val accessTokenPreview: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}