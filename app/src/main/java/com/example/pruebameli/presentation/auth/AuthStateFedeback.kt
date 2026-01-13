package com.example.pruebameli.presentation.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag


@Composable
fun AuthStateFeedback(state: AuthUiState) {
    when (state) {
        is AuthUiState.Success -> {
            Text(
                text = "Token válido",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("auth_success")
            )
        }

        is AuthUiState.Error -> {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("auth_error")
            )
        }

        else -> Unit
    }
}
