package com.example.pruebameli.presentation.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable




@Composable
fun AuthHeader(hasSession: Boolean) {
    Text(
        text = if (hasSession) "Sesión activa" else "Autenticación",
        style = MaterialTheme.typography.headlineSmall
    )

    Text(
        text = if (hasSession) {
            "Ya tienes una sesión válida."
        } else {
            "Conéctate con Mercado Libre para continuar."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
