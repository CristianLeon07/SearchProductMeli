package com.example.pruebameli.domain.auth

import com.example.pruebameli.domain.repository.AuthRepository
import com.example.pruebameli.domain.repository.TokenStorage


import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthManager @Inject constructor(
    private val storage: TokenStorage,
    private val repository: AuthRepository
) {

    /**
     * Mutex evita que si llegan 3 requests al mismo tiempo y el token venció,
     * NO se ejecuten 3 refresh en paralelo (eso rompe y genera errores raros).
     *
     * Con Mutex: 1 refresh máximo a la vez, el resto espera.
     */
    private val refreshMutex = Mutex()

    /**
     * Devuelve un access token válido para usar en el header Authorization.
     *
     * Reglas:
     * - Si NO hay refresh_token => el usuario nunca hizo login => retorna null.
     * - Si el access token “ya expiró” (según tu ventana de 25 min) => refresca y guarda.
     * - Si aún es válido => retorna el token guardado.
     */
    suspend fun getValidAccessToken(): String? {
        val refreshToken = storage.getRefreshToken()
        if (refreshToken.isNullOrBlank()) return null

        if (!storage.shouldRefresh()) {
            return storage.getAccessToken()
        }

        return refreshMutex.withLock {
            if (!storage.shouldRefresh()) {
                return@withLock storage.getAccessToken()
            }

            repository.refreshAndSaveToken().getOrThrow()

            storage.getAccessToken()
        }
    }
}
