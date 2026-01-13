package com.example.pruebameli.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.pruebameli.R

@Composable
fun EmptySearchIllustration(modifier: Modifier = Modifier) {
    // Android seleccionará automáticamente entre drawable y drawable-night
    // si tienes dos versiones de la imagen
    Image(
        painter = painterResource(id = R.drawable.search_meli),
        contentDescription = "Ilustración de búsqueda vacía",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
