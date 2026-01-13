package com.example.pruebameli.domain.usecase.auth

import com.example.pruebameli.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso que intercambia el código de autorización por tokens de acceso.
 *
 * Este UseCase:
 * - Valida que el código no esté vacío
 * - Llama al repositorio para intercambiar el código
 * - Maneja errores de validación
 *
 * Se ejecuta UNA SOLA VEZ después de que el usuario autoriza la aplicación
 * en el proveedor OAuth.
 *
 * Ejemplo de uso:
 * ```kotlin
 * val result = exchangeCodeForTokenUseCase(authorizationCode)
 * result.onSuccess { /* Navegar a home */ }
 * result.onFailure { /* Mostrar error */ }
 * ```
 */
class ExchangeCodeForTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Intercambia el código de autorización por tokens de acceso.
     *
     * @param code Código de autorización recibido del proveedor OAuth
     * @return Result<Unit> éxito o error con mensaje descriptivo
     */
    suspend operator fun invoke(code: String): Result<Unit> {
        // Validación de negocio
        if (code.isBlank()) {
            return Result.failure(
                IllegalArgumentException("El código de autorización no puede estar vacío")
            )
        }
        
        // Delegamos al repositorio
        return repository.exchangeCodeAndSaveToken(code)
    }
}
