package com.example.pruebameli.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.pruebameli.domain.network.NetworkMonitor
import com.example.pruebameli.domain.usecase.SearchProductsPagedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchProductsPaged: SearchProductsPagedUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    companion object {
        private const val TAG = "HOME_VM"
    }

    // 🔌 Estado de conectividad de red
    // Se convierte a StateFlow para que Compose pueda observarlo
    val isConnected: StateFlow<Boolean> = networkMonitor.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true // Asume conectado inicialmente
        )

    private val _queryText = MutableStateFlow("")
    val queryText: StateFlow<String> = _queryText.asStateFlow()

    private val _submittedQuery = MutableStateFlow("")
    
    // Estado para saber si el usuario ya realizó alguna búsqueda
    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    fun onQueryChange(text: String) {
        Log.v(TAG, "✏️  Query cambiado: '$text'")
        _queryText.value = text
    }

    fun onSearchClick() {
        val trimmedQuery = _queryText.value.trim()
        
        Log.i(TAG, "🔍 Usuario ejecutó búsqueda")
        Log.d(TAG, "   - Query: '$trimmedQuery'")
        
        if (trimmedQuery.isEmpty()) {
            Log.w(TAG, "⚠️  Búsqueda ignorada - Query vacío")
            return
        }
        
        if (trimmedQuery.isNotEmpty()) {
            Log.i(TAG, "✅ Iniciando búsqueda de productos: '$trimmedQuery'")
            // Marca que el usuario ya realizó una búsqueda
            _hasSearched.value = true
            // Al cambiar submittedQuery, flatMapLatest cancela la búsqueda anterior
            // y crea un nuevo flujo paginado para el nuevo query.
            _submittedQuery.value = trimmedQuery
        }
    }

    val products = _submittedQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                Log.d(TAG, "📭 Query vacío - Retornando PagingData vacío")
                // Si no hay query, no buscamos nada
                flowOf(PagingData.empty())
            } else {
                Log.d(TAG, "🚀 Creando nuevo flujo paginado para: '$query'")
                // Nuevo Flow<PagingData> por cada búsqueda
                searchProductsPaged(query)
            }
        }
        // Cachea dentro del scope del VM (rotación / recomposición)
        .cachedIn(viewModelScope)
}
