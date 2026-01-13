package com.example.pruebameli.presentation.navigation

import kotlinx.serialization.Serializable


@Serializable
sealed interface ScreenDest {

    @Serializable
    data object Auth : ScreenDest

    @Serializable
    data object Home : ScreenDest

    @Serializable
    data class Detail(val id: String) : ScreenDest
}

