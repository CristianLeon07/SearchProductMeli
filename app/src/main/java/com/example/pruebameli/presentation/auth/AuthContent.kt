package com.example.pruebameli.presentation.auth

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag



@Composable
fun AuthContent(
    state: AuthUiState,
    hasSession: Boolean,
    onLoginClick: () -> Unit
) {
    when {
        hasSession -> {
            SessionActiveState()
        }

        state is AuthUiState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier.testTag("auth_loading")
            )
        }

        else -> {
            LoginButton(onLoginClick)
        }
    }

    AuthStateFeedback(state)
}
