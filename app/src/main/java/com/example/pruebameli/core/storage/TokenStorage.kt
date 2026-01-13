package com.example.pruebameli.core.storage

import android.util.Log
import com.example.pruebameli.domain.config.AppConfig
import com.example.pruebameli.domain.repository.TokenStorage
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "auth_store")

/**
 * Implementación de TokenStorage usando DataStore (persistencia segura y asíncrona).
 *
 * Guarda:
 * - access_token: token usado en cada request
 * - refresh_token: token persistente para renovar sesión sin pedir login
 * - expires_at: momento en que el access_token debe refrescarse
 */
class TokenStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenStorage {

    companion object {
        private const val TAG = "TOKEN_STORAGE"
    }

    /** Clave para guardar el access token */
    private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")

    /** Clave para guardar el refresh token */
    private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    /** Clave para guardar el tiempo de expiración del access token */
    private val KEY_EXPIRES_AT = longPreferencesKey("expires_at_epoch_seconds")

    /** Intervalo fijo para refrescar el token (configurado en Domain) */
    private val REFRESH_INTERVAL_SECONDS = AppConfig.Auth.TOKEN_REFRESH_WINDOW_SECONDS

    /**
     * Guarda los tokens de autenticación y calcula la fecha de expiración.
     *
     * Se llama:
     * - después del primer login (exchange code → token)
     * - después de cada refresh exitoso
     *
     * @param accessToken token usado para autenticar requests
     * @param refreshToken token persistente para renovar sesión
     */
    override suspend fun save(
        accessToken: String,
        refreshToken: String
    ) {
        val now = System.currentTimeMillis() / 1000
        val expiresAt = now + REFRESH_INTERVAL_SECONDS
        
        val expiresAtDate = Date(expiresAt * 1000)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        Log.i(TAG, "Guardando tokens en DataStore")
        Log.d(TAG, "Access token: ${accessToken.take(20)}...")
        Log.d(TAG, "Refresh token: ${refreshToken.take(20)}...")
        Log.d(TAG, "Expira en: ${dateFormat.format(expiresAtDate)} (${REFRESH_INTERVAL_SECONDS}s)")

        try {
            context.dataStore.edit { prefs ->
                prefs[KEY_ACCESS_TOKEN] = accessToken
                prefs[KEY_REFRESH_TOKEN] = refreshToken
                prefs[KEY_EXPIRES_AT] = expiresAt
            }
            Log.i(TAG, "✅ Tokens guardados exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar tokens: ${e.message}", e)
            throw e
        }
    }

    /**
     * Indica si el access token ya expiró y debe refrescarse.
     *
     * Se usa antes de hacer una request protegida.
     *
     * @return true si ya pasaron los 25 minutos y toca refrescar
     */
    override suspend fun shouldRefresh(): Boolean {
        try {
            val prefs = context.dataStore.data.first()
            val expiresAt = prefs[KEY_EXPIRES_AT] ?: 0L
            val now = System.currentTimeMillis() / 1000
            val shouldRefresh = now >= expiresAt
            
            if (shouldRefresh) {
                val timeExpired = now - expiresAt
                Log.d(TAG, "⏰ Token expirado hace ${timeExpired}s - Requiere refresh")
            } else {
                val timeRemaining = expiresAt - now
                Log.d(TAG, "✓ Token vigente - Expira en ${timeRemaining}s")
            }
            
            return shouldRefresh
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al verificar expiración: ${e.message}", e)
            return true // Por seguridad, asumimos que debe refrescar
        }
    }

    /**
     * Obtiene el access token almacenado.
     *
     * Se usa en el Interceptor para agregar el header Authorization.
     *
     * @return access token o null si no existe
     */
    override suspend fun getAccessToken(): String? {
        return try {
            val prefs = context.dataStore.data.first()
            val token = prefs[KEY_ACCESS_TOKEN]
            if (token != null) {
                Log.d(TAG, "📖 Access token leído: ${token.take(20)}...")
            } else {
                Log.w(TAG, "⚠️  Access token no encontrado en storage")
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al leer access token: ${e.message}", e)
            null
        }
    }

    /**
     * Obtiene el refresh token almacenado.
     *
     * Si este valor existe, el usuario ya se autenticó al menos una vez
     * y NO se debe mostrar nuevamente el botón de login.
     *
     * @return refresh token o null si nunca hubo login
     */
    override suspend fun getRefreshToken(): String? {
        return try {
            val prefs = context.dataStore.data.first()
            val token = prefs[KEY_REFRESH_TOKEN]
            if (token != null) {
                Log.d(TAG, "📖 Refresh token leído: ${token.take(20)}...")
            } else {
                Log.w(TAG, "⚠️  Refresh token no encontrado - Usuario no autenticado")
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al leer refresh token: ${e.message}", e)
            null
        }
    }


    /**
     * Flow reactivo que indica si el usuario ya se autenticó.
     *
     * - la UI se actualiza automáticamente cuando cambia el estado
     * - permite ocultar o mostrar botones automaticamente
     *
     * @return Flow<Boolean> true si hay sesión iniciada
     */
    override fun isUserAuthenticatedOnceFlow(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            !prefs[KEY_REFRESH_TOKEN].isNullOrBlank()
        }
    }
}
