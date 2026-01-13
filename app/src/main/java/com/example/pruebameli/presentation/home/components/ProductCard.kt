package com.example.pruebameli.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pruebameli.domain.models.Product
import com.example.pruebameli.ui.theme.PruebaMeliTheme


@Composable
fun ProductItemCard(
    product: Product,
    onClick: () -> Unit
) {

    val subtitle = remember(product.brand, product.model) {
        listOfNotNull(product.brand, product.model)
            .joinToString(" · ")
            .ifBlank { null }
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {

            // Imagen arriba (proporción tipo catálogo)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // cuadrada como Meli
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Imagen real si existe
                if (!product.imageUrl.isNullOrBlank()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {

                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = "Imagen de ${product.name}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                    }
                }

                // Favorito (arriba derecha)
                IconButton(
                    onClick = { /* futuro: favoritos */ },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Botón carrito flotante (abajo derecha)
                IconButton(
                    onClick = { /* futuro: agregar */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Agregar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ---------- Texto abajo ----------
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                // Título: tamaño y peso “meli-like”
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ===== PREVIEWS =====

@PreviewLightDark
@Composable
private fun ProductItemCardPreview() {
    PruebaMeliTheme {
        ProductItemCard(
            product = Product(
                id = "MLA123456789",
                name = "iPhone 15 Pro Max 256GB - Titanio Natural",
                brand = "Apple",
                model = "A2849",
                imageUrl = "https://http2.mlstatic.com/D_NQ_NP_2X_123456-MLA123456789-123456-F.webp"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductItemCardWithoutImagePreview() {
    PruebaMeliTheme {
        ProductItemCard(
            product = Product(
                id = "MLA987654321",
                name = "Notebook Lenovo IdeaPad Gaming 3 15.6 pulgadas",
                brand = "Lenovo",
                model = "15ACH6",
                imageUrl = null
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductItemCardWithoutBrandPreview() {
    PruebaMeliTheme {
        ProductItemCard(
            product = Product(
                id = "MLA555555555",
                name = "Zapatillas Deportivas Running Muy Cómodas Para Todo El Día",
                brand = null,
                model = null,
                imageUrl = null
            ),
            onClick = {}
        )
    }
}
