package com.example.pruebameli.presentation.auth



sealed interface AuthEvent {
    data object NavigateHome : AuthEvent
}
