package com.example.pruebameli.domain.repository

/**
 * Interfaz del repositorio de autenticación en Domain layer.
 * Define las operaciones de autenticación sin depender de implementaciones de Data layer.
 */
interface AuthRepository {
    /**
     * Intercambia el código de autorización por tokens de acceso.
     * Guarda los tokens internamente.
     *
     * @param code Código de autorización recibido del OAuth provider
     * @return Result<Unit> que indica éxito o fallo de la operación
     */
    suspend fun exchangeCodeAndSaveToken(code: String): Result<Unit>

    /**
     * Renueva el access token usando el refresh token guardado.
     * Guarda los nuevos tokens internamente.
     *
     * @return Result<Unit> que indica éxito o fallo de la operación
     */
    suspend fun refreshAndSaveToken(): Result<Unit>
}
