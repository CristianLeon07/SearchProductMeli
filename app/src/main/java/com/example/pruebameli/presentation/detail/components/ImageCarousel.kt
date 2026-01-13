package com.example.pruebameli.presentation.detail.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pruebameli.ui.theme.PruebaMeliTheme

@Composable
fun ImageCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    // Si no hay imágenes, no renderizamos nada (evita UI vacía)
    if (imageUrls.isEmpty()) return


    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    Column(modifier = modifier) {

        // Carrusel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = imageUrls[page],
                    contentDescription = "Imagen ${page + 1} de ${imageUrls.size}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }


        Spacer(Modifier.height(10.dp))

        // Indicadores (dots)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(imageUrls.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

// ===== PREVIEWS =====

@PreviewLightDark
@Composable
private fun ImageCarouselSingleImagePreview() {
    PruebaMeliTheme {
        ImageCarousel(
            imageUrls = listOf(
                "https://http2.mlstatic.com/D_NQ_NP_2X_123456-MLA123456789-123456-F.webp"
            )
        )
    }
}

@PreviewLightDark
@Composable
private fun ImageCarouselMultipleImagesPreview() {
    PruebaMeliTheme {
        ImageCarousel(
            imageUrls = listOf(
                "https://http2.mlstatic.com/D_NQ_NP_2X_123456-MLA123456789-123456-F.webp",
                "https://http2.mlstatic.com/D_NQ_NP_2X_789012-MLA123456789-123456-F.webp",
                "https://http2.mlstatic.com/D_NQ_NP_2X_345678-MLA123456789-123456-F.webp"
            )
        )
    }
}
