package com.example.pruebameli.data.auth


import android.net.Uri
import androidx.core.net.toUri
import com.example.pruebameli.core.config.MeliAuthConfig
import java.util.UUID

data class AuthRequest(
    val url: Uri,
    val state: String
)

object AuthUrlBuilder {

    fun build(): AuthRequest {
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
