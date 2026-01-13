package com.example.pruebameli.domain.network

import kotlinx.coroutines.flow.Flow

/**
 * Interface para monitorear el estado de conectividad de red.
 * 
 * Esta abstracción permite que ViewModels y UseCases observen cambios
 * en la conectividad sin depender directamente de APIs de Android.
 * 
 * Siguiendo Clean Architecture:
 * - Esta interfaz vive en la capa de dominio
 * - No tiene dependencias de Android Framework
 * - Puede ser fácilmente mockeada en tests
 */
interface NetworkMonitor {
    
    /**
     * Flow que emite `true` cuando hay conexión a internet disponible,
     * y `false` cuando no hay conexión.
     * 
     * Este Flow es HOT (compartido) y permanece activo mientras haya
     * colectores suscritos. Emite el estado inicial inmediatamente
     * al suscribirse y luego emite cada cambio de conectividad.
     * 
     * @return Flow<Boolean> - true = conectado, false = sin conexión
     */
    val isConnected: Flow<Boolean>
}
