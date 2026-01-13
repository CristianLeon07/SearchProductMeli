package com.example.pruebameli.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pruebameli.R
import com.example.pruebameli.ui.theme.PruebaMeliTheme

/**
 * Componente Compose que muestra un estado de "Sin conexión a internet".
 *
 * Características:
 * - Ilustración visual clara (usa el launcher icon como placeholder)
 * - Mensaje descriptivo
 * - Botón de reintentar opcional
 * - Diseño centrado y responsive
 *
 * @param modifier Modificador para personalizar el layout
 * @param title Título del mensaje de error (por defecto "Sin conexión")
 * @param message Mensaje descriptivo del error
 * @param showRetryButton Si se debe mostrar el botón de reintentar
 * @param onRetryClick Callback cuando se presiona el botón de reintentar
 */
@Composable
fun NoInternetView(
    modifier: Modifier = Modifier,
    title: String = "Sin conexión a internet",
    message: String = "Por favor, verifica tu conexión e intenta nuevamente.",
    showRetryButton: Boolean = true,
    onRetryClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.mipmap.failure_network_meli),
            contentDescription = "Sin conexión a internet",
            modifier = Modifier.size(250.dp),
            alpha = 0.6f
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Título
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mensaje descriptivo
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

    }
}

