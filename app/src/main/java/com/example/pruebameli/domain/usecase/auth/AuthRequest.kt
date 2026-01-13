package com.example.pruebameli.domain.usecase.auth

import android.net.Uri

/**
 * Datos de una solicitud de autenticación OAuth.
 *
 * @property url URL completa para redirigir al usuario al proveedor OAuth
 * @property state Token único para validar la respuesta (previene ataques CSRF)
 */
data class AuthRequest(
    val url: Uri,
    val state: String
)
