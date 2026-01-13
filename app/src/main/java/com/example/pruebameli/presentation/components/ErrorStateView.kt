package com.example.pruebameli.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.pruebameli.ui.theme.PruebaMeliTheme


@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Ocurrió un problema",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

// ===== PREVIEWS =====

@PreviewLightDark
@Composable
private fun ErrorStatePreview() {
    PruebaMeliTheme {
        ErrorState(
            message = "No se pudo conectar al servidor. Verifica tu conexión a internet.",
            onRetry = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ErrorStateShortMessagePreview() {
    PruebaMeliTheme {
        ErrorState(
            message = "Error de red",
            onRetry = {}
        )
    }
}
