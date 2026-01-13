package com.example.pruebameli.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.pruebameli.domain.models.Product
import com.example.pruebameli.presentation.components.EmptyState
import com.example.pruebameli.presentation.components.ErrorState


@Composable
fun HomeContent(
    products: LazyPagingItems<Product>,
    gridState: LazyGridState,
    onProductClick: (String) -> Unit,
    isConnected: Boolean = true
) {
    // Estado de carga inicial (primera página / refresh)
    val refreshState = products.loadState.refresh
    // Estado de carga de paginación (siguientes páginas / append)
    val appendState = products.loadState.append
    
    // Calcular si se llegó al final de los resultados
    val endReached by remember {
        derivedStateOf {
            appendState is LoadState.NotLoading &&
                    appendState.endOfPaginationReached &&
                    products.itemCount > 0
        }
    }

    // LOADING inicial: mostramos skeleton grid completo
    if (refreshState is LoadState.Loading) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Skeletons para simular cards mientras llega la data real
            items(6) { ProductSkeleton() }
        }
        return
    }

    // ERROR inicial: falló la primera carga (refresh)
    // PERO si hay conexión activa, mostrar skeleton en lugar de error
    if (refreshState is LoadState.Error) {
        if (isConnected) {
            // Si hay conexión, mostrar skeleton mientras se reintenta
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(6) { ProductSkeleton() }
            }
            return
        } else {
            // Sin conexión, mostrar el error normalmente
            val e = refreshState.error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                ErrorState(
                    message = e.message ?: "Error desconocido",
                    onRetry = { products.retry() }
                )
            }
            return
        }
    }

    // Si llegamos aquí, refresh ya terminó correctamente
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // Render de items paginados
        items(
            count = products.itemCount,
            key = { index -> products[index]?.id ?: index }
        ) { index ->
            val item = products[index]

            if (item != null) {
                ProductItemCard(
                    product = item,
                    onClick = { onProductClick(item.id) }
                )
            }
        }

        // EMPTY STATE: solo cuando no hay items y refresh terminó
        if (products.itemCount == 0 && refreshState is LoadState.NotLoading) {
            item(span = { GridItemSpan(2) }) {
                EmptyState()
            }
        }

        // LOADING de paginación: se está cargando la siguiente página
        if (appendState is LoadState.Loading) {
            item(span = { GridItemSpan(2) }) {
                LoadingMoreRow()
            }
        }

        // ERROR de paginación: falló traer la siguiente página
        // PERO si hay conexión activa, ocultarlo (se reintentará automáticamente)
        if (appendState is LoadState.Error && !isConnected) {
            item(span = { GridItemSpan(2) }) {
                RetryAppendRow { products.retry() }
            }
        }

        // FIN DE LISTA: mensaje sutil al final cuando ya no hay más páginas
        if (endReached) {
            item(span = { GridItemSpan(2) }) {
                EndOfResultsRowAnimated()
            }
        }
    }
}


@Composable
fun EndOfResultsRowAnimated() {
    AnimatedVisibility(
        visible = true,
        // Fade + slide suave desde abajo
        enter = fadeIn() + slideInVertically { fullHeight -> fullHeight / 3 }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = "No hay más resultados",
                modifier = Modifier.padding(horizontal = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}


