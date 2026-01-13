package com.example.pruebameli.domain.usecase.auth

import com.example.pruebameli.domain.repository.TokenStorage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso que obtiene el estado de autenticación del usuario.
 *
 * Este UseCase:
 * - Retorna un Flow reactivo que emite true/false según el estado de autenticación
 * - Permite a la UI reaccionar automáticamente a cambios en el estado
 * - No requiere polling manual
 *
 * El usuario está autenticado si existe un refresh_token guardado.
 */
class GetAuthStateUseCase @Inject constructor(
    private val storage: TokenStorage
) {
    /**
     * Obtiene un Flow del estado de autenticación.
     *
     * @return Flow<Boolean> que emite true si hay sesión activa, false en caso contrario
     */
    operator fun invoke(): Flow<Boolean> {
        return storage.isUserAuthenticatedOnceFlow()
    }
}
