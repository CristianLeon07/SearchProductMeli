package com.example.pruebameli.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.pruebameli.presentation.navigation.ScreenDest

@Composable
fun HomeRoute(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val query by viewModel.queryText.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val products = viewModel.products.collectAsLazyPagingItems()
    
    // Evita recomposiciones innecesarias si cambia la instancia de navController
    val currentNavController by rememberUpdatedState(navController)

    // Estado para trackear la conexión anterior
    var wasDisconnected by remember { mutableStateOf(false) }

    // Detecta cuando se recupera la conexión a internet
    LaunchedEffect(isConnected, hasSearched) {
        if (isConnected && wasDisconnected && hasSearched) {
            // Se recuperó la conexión después de haberla perdido
            // y el usuario ya había realizado una búsqueda
            products.refresh()
        }
        
        // Actualiza el estado de conexión anterior
        wasDisconnected = !isConnected
    }

    HomeScreen(
        query = query,
        products = products,
        hasSearched = hasSearched,
        isConnected = isConnected,
        onQueryChange = viewModel::onQueryChange,
        onSearch = {
            viewModel.onSearchClick()
            products.refresh()
        },
        onProductClick = { id ->
            currentNavController.navigate(ScreenDest.Detail(id))
        },
        onRetryConnection = {
            // Reintenta la búsqueda al reconectar
            products.refresh()
        }
    )
}

