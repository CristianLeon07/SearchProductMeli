package com.example.pruebameli.presentation.detail

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pruebameli.core.utils.ResourceUiState
import com.example.pruebameli.presentation.components.MeliScaffold
import com.example.pruebameli.presentation.components.NoInternetView
import com.example.pruebameli.presentation.detail.components.ImageCarousel

@Composable
fun DetailProductScreen(
    productId: String,
    onBack: () -> Unit,
    detailProductViewModel: DetailProductViewModel = hiltViewModel()
) {
    val state by detailProductViewModel.state.collectAsState()
    val isConnected by detailProductViewModel.isConnected.collectAsState()

    // Cargar detalle 1 sola vez al entrar o si cambia el id
    LaunchedEffect(productId) {
        detailProductViewModel.load(productId)
    }

    MeliScaffold(
        title = "Detalle",
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }

        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 🔌 Mostrar vista de sin conexión si no hay internet
            if (!isConnected) {
                NoInternetView(
                    onRetryClick = { detailProductViewModel.load(productId) }
                )
            } else {
                when (val s = state) {
                    ResourceUiState.Idle -> Unit

                    ResourceUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is ResourceUiState.Error -> {
                        // Mostrar error genérico (no relacionado con red)
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = s.message,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { detailProductViewModel.load(productId) }) {
                                Text("Reintentar")
                            }
                        }
                    }

                    is ResourceUiState.Success -> {
                        val detail = s.data

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Carrusel con imágenes del backend
                            ImageCarousel(
                                imageUrls = detail.imageUrls,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Nombre
                            Text(
                                text = detail.name,
                                style = MaterialTheme.typography.headlineSmall
                            )

                            // Descripción
                            if (!detail.description.isNullOrBlank()) {
                                Text(
                                    text = "Descripción",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = detail.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

