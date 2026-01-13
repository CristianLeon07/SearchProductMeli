package com.example.pruebameli.presentation.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp



@Composable
fun LoginButton(
    onLoginClick: () -> Unit
) {
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("auth_login_button"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Autenticar con Meli",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
