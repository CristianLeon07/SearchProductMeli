package com.example.pruebameli.domain.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementación fake de NetworkMonitor para tests.
 * 
 * Permite controlar manualmente el estado de conectividad
 * durante las pruebas unitarias.
 * 
 * Ejemplo de uso:
 * ```kotlin
 * @Test
 * fun `when no internet, shows error`() = runTest {
 *     val fakeNetwork = FakeNetworkMonitor()
 *     val viewModel = HomeViewModel(useCase, fakeNetwork)
 *     
 *     // Simular pérdida de conexión
 *     fakeNetwork.setConnected(false)
 *     
 *     // Verificar que el estado cambió
 *     assertFalse(viewModel.isConnected.value)
 * }
 * ```
 */
class FakeNetworkMonitor : NetworkMonitor {
    
    private val _isConnected = MutableStateFlow(true)
    override val isConnected: Flow<Boolean> = _isConnected
    
    /**
     * Simula un cambio en el estado de conectividad.
     * 
     * @param connected true = conectado, false = sin conexión
     */
    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
    
    /**
     * Obtiene el estado actual de conectividad de forma síncrona.
     * Útil para verificaciones rápidas en tests.
     */
    fun isCurrentlyConnected(): Boolean = _isConnected.value
}
