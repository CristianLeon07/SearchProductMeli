package com.example.pruebameli.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.example.pruebameli.domain.models.Product
import com.example.pruebameli.presentation.components.MeliScaffold
import com.example.pruebameli.presentation.components.NoInternetView
import com.example.pruebameli.presentation.home.components.EmptySearchIllustration
import com.example.pruebameli.presentation.home.components.HomeContent
import com.example.pruebameli.presentation.home.components.SearchBarMeli

@Composable
fun HomeScreen(
    query: String,
    products: LazyPagingItems<Product>,
    hasSearched: Boolean,
    isConnected: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onProductClick: (String) -> Unit,
    onRetryConnection: () -> Unit = {},
) {
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    MeliScaffold(
        title = "Mercado Libre"
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            SearchBarMeli(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch
            )

            // 🔌 Mostrar vista de sin conexión si no hay internet
            if (!isConnected) {
                NoInternetView(
                    onRetryClick = onRetryConnection
                )
            }
            // Mostrar ilustración de bienvenida si no se ha realizado ninguna búsqueda
            else if (!hasSearched) {
                WelcomeEmptyState()
            } else {
                // Mostrar resultados de búsqueda
                val gridState = rememberSaveable(
                    saver = LazyGridState.Saver
                ) { LazyGridState() }

                HomeContent(
                    products = products,
                    gridState = gridState,
                    onProductClick = onProductClick,
                    isConnected = isConnected
                )
            }
        }
    }
}

@Composable
private fun WelcomeEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmptySearchIllustration(
                modifier = Modifier.fillMaxSize()
            )

        }
    }
}
