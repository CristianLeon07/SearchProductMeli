package com.example.pruebameli.presentation.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebameli.core.utils.ResourceUiState
import com.example.pruebameli.core.utils.toUiState
import com.example.pruebameli.domain.models.ProductDetail
import com.example.pruebameli.domain.network.NetworkMonitor
import com.example.pruebameli.domain.usecase.GetProductDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel para la pantalla de detalle de producto.
 *
 * Maneja:
 * - Carga del detalle del producto
 * - Estados de UI (Loading, Success, Error)
 * - Navegación de estados
 * - Validación de conexión a internet
 *
 * El UseCase ya se encarga de:
 * - Validaciones de ID
 * - Manejo de errores de red
 * - Transformación a ResourceData
 */
@HiltViewModel
class DetailProductViewModel @Inject constructor(
    private val getProductDetail: GetProductDetailUseCase,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    companion object {
        private const val TAG = "DETAIL_VM"
    }

    // 🔌 Estado de conectividad de red
    val isConnected: StateFlow<Boolean> = networkMonitor.isConnected
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _state = MutableStateFlow<ResourceUiState<ProductDetail>>(ResourceUiState.Idle)
    val state = _state.asStateFlow()

    /**
     * Carga el detalle de un producto.
     *
     * Verifica primero si hay conexión a internet antes de intentar cargar.
     * Si no hay conexión, emite un estado de error apropiado.
     *
     * @param id ID del producto a cargar
     */
    fun load(id: String) = viewModelScope.launch {
        // ✅ Verificar conexión antes de hacer la petición
        if (!isConnected.value) {
            Log.w(TAG, "⚠️ Sin conexión a internet - No se puede cargar el detalle")
            _state.value = ResourceUiState.Error("Sin conexión a internet. Por favor, verifica tu conexión.")
            return@launch
        }
        
        _state.value = ResourceUiState.Loading
        Log.d(TAG, "📦 Cargando detalle del producto: $id")
        
        // El UseCase ya maneja validaciones y errores
        val result = getProductDetail(id)
        
        // Transformamos ResourceData (Domain) a ResourceUiState (Presentation)
        _state.value = result.toUiState()
    }
}
