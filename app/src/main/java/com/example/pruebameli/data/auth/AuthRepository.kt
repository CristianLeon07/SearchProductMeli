package com.example.pruebameli.data.auth

import android.util.Log
import com.example.pruebameli.core.config.MeliAuthConfig
import com.example.pruebameli.domain.repository.AuthRepository
import com.example.pruebameli.domain.repository.TokenStorage
import javax.inject.Inject


class AuthRepositoryImpl @Inject constructor(
    private val api: OAuthApi,
    private val storage: TokenStorage
) : AuthRepository {

    companion object {
        private const val TAG = "AUTH_REPO"
    }

    /**
     * LOGIN INICIAL (solo 1 vez por usuario):
     * Intercambia el "authorization code" por:
     * - access_token (para requests)
     * - refresh_token (para renovar sin volver a login)
     *
     * Debe llamarse SOLO cuando recibes el code por el redirect.
     */
    override suspend fun exchangeCodeAndSaveToken(code: String): Result<Unit> = runCatching {
        Log.i(TAG, " Iniciando exchangeCodeForToken")
        Log.d(TAG, "Code recibido: ${code.take(10)}...")
        
        val response = api.exchangeCodeForToken(
            grantType = MeliAuthConfig.GRANT_TYPE,
            clientId = MeliAuthConfig.CLIENT_ID,
            clientSecret = MeliAuthConfig.CLIENT_SECRET,
            code = code,
            redirectUri = MeliAuthConfig.REDIRECT_URI
        )

        // Si HTTP no es 2xx o body es null, lanzamos error con detalle
        val token = response.requireBodyOrThrow("exchangeCodeForToken")
        Log.d(TAG, "Exchange exitoso - HTTP ${response.code()}")

        // Importante: si refresh_token viene null/vacío, no sirve para mantener sesión
        val refreshToken = token.refresh_token.orEmpty()
        if (refreshToken.isBlank()) {
            Log.e(TAG, "refresh_token vacío en respuesta exitosa")
            throw IllegalStateException("OAuth exchange succeeded but refresh_token is empty")
        }

        Log.d(TAG, "Guardando tokens en storage")
        Log.d(TAG, "   - Access token: ${token.access_token.take(20)}...")
        Log.d(TAG, "   - Refresh token: ${refreshToken.take(20)}...")
        
        // Guardamos en DataStore; TokenStorage calcula el "expiresAt" (25 min)
        storage.save(
            accessToken = token.access_token,
            refreshToken = refreshToken
        )

        Log.i(TAG, "exchangeCodeForToken completado exitosamente")
        Unit
    }.onFailure { error ->
        Log.e(TAG, "Error en exchangeCodeForToken: ${error.message}", error)
    }

    /**
     * REFRESH (después del primer login):
     * Usa el refresh_token guardado para pedir un nuevo access_token.
     *
     * NO requiere code.
     * Se puede llamar cada vez que el access token esté "vencido" (cada 25 min).
     */
    override suspend fun refreshAndSaveToken(): Result<Unit> = runCatching {
        Log.i(TAG, "Iniciando refreshToken")
        
        // Si no hay refresh_token, significa que el usuario nunca hizo login
        val storedRefreshToken = storage.getRefreshToken()
            ?.takeIf { it.isNotBlank() }
            ?: run {
                Log.e(TAG, "No hay refresh_token almacenado")
                throw IllegalStateException("No refresh_token stored. User must authenticate once.")
            }

        Log.d(TAG, "📝 Usando refresh_token: ${storedRefreshToken.take(20)}...")

        val response = api.refreshToken(
            grantType = MeliAuthConfig.GRANT_TYPE_REFRESH,
            clientId = MeliAuthConfig.CLIENT_ID,
            clientSecret = MeliAuthConfig.CLIENT_SECRET,
            refresh_token = storedRefreshToken
        )

        val token = response.requireBodyOrThrow("refreshToken")
        Log.d(TAG, "Refresh exitoso - HTTP ${response.code()}")

        // Algunos providers rotan refresh_token (devuelven uno nuevo); otros no.
        val newRefreshToken = token.refresh_token?.takeIf { it.isNotBlank() } ?: storedRefreshToken
        
        if (token.refresh_token != null && token.refresh_token != storedRefreshToken) {
            Log.d(TAG, "Provider rotó el refresh_token - Usando nuevo")
        } else {
            Log.d(TAG, "Reutilizando refresh_token anterior")
        }

        Log.d(TAG, "Guardando tokens actualizados")
        // Guardamos el nuevo access_token y el refresh_token (nuevo o el anterior)
        storage.save(
            accessToken = token.access_token,
            refreshToken = newRefreshToken
        )

        Log.i(TAG, "refreshToken completado exitosamente")
        Unit
    }.onFailure { error ->
        Log.e(TAG, "Error en refreshToken: ${error.message}", error)
    }
}

/**
 * Extensión para evitar repetir:
 * - if(!isSuccessful) ...
 * - body() ?: ...
 *
 * Hace que el repository sea más limpio y uniforme para manejo de errores.
 */
private fun <T> retrofit2.Response<T>.requireBodyOrThrow(tag: String): T {
    if (!isSuccessful) {
        val httpCode = code()
        val errorBody = errorBody()?.string()
        Log.e("AUTH_REPO", "OAuth $tag falló - HTTP $httpCode")
        Log.e("AUTH_REPO", "Error body: ${errorBody ?: "empty"}")
        throw IllegalStateException("OAuth $tag failed. HTTP $httpCode. Body=${errorBody ?: "empty"}")
    }
    val body = body()
    if (body == null) {
        Log.e("AUTH_REPO", "OAuth $tag - Response body es null")
        throw IllegalStateException("OAuth $tag response body is null")
    }
    return body
}
