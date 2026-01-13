package com.example.pruebameli.domain.usecase.auth

import android.net.Uri
import androidx.core.net.toUri
import com.example.pruebameli.core.config.MeliAuthConfig
import java.util.UUID
import javax.inject.Inject

/**
 * Caso de uso que construye la URL de autenticación OAuth.
 *
 * Genera:
 * - URL completa con todos los parámetros necesarios para el flujo OAuth
 * - State token único para prevenir ataques CSRF
 */
class BuildAuthUrlUseCase @Inject constructor() {
    
    /**
     * Construye una solicitud de autenticación OAuth.
     *
     * @return AuthRequest con la URL completa y el state token
     */
    operator fun invoke(): AuthRequest {
        val state = UUID.randomUUID().toString()
        
        val url = MeliAuthConfig.AUTH_BASE_URL.toUri().buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", MeliAuthConfig.CLIENT_ID)
            .appendQueryParameter("redirect_uri", MeliAuthConfig.REDIRECT_URI)
            .appendQueryParameter("state", state)
            .build()
        
        return AuthRequest(url = url, state = state)
    }
}
