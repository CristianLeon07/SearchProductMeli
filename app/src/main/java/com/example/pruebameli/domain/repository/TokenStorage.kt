package com.example.pruebameli.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del almacenamiento de tokens en Domain layer.
 * Define las operaciones de almacenamiento sin depender de implementaciones de Android.
 */
interface TokenStorage {
    /**
     * Guarda los tokens de autenticación y calcula la fecha de expiración.
     *
     * @param accessToken token usado para autenticar requests
     * @param refreshToken token persistente para renovar sesión
     */
    suspend fun save(
        accessToken: String,
        refreshToken: String
    )

    /**
     * Indica si el access token ya expiró y debe refrescarse.
     *
     * @return true si ya pasaron los 25 minutos y toca refrescar
     */
    suspend fun shouldRefresh(): Boolean

    /**
     * Obtiene el access token almacenado.
     *
     * @return access token o null si no existe
     */
    suspend fun getAccessToken(): String?

    /**
     * Obtiene el refresh token almacenado.
     *
     * @return refresh token o null si nunca hubo login
     */
    suspend fun getRefreshToken(): String?

    /**
     * Flow reactivo que indica si el usuario ya se autenticó.
     *
     * @return Flow<Boolean> true si hay sesión iniciada
     */
    fun isUserAuthenticatedOnceFlow(): Flow<Boolean>
}
