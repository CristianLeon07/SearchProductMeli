package com.example.pruebameli.presentation.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag




@Composable
fun SessionActiveState() {
    Text(
        text = "No necesitas autenticarte nuevamente.",
        modifier = Modifier.testTag("auth_session_active"),
        style = MaterialTheme.typography.bodyMedium
    )
}

