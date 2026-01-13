package com.example.pruebameli.data.network.interceptor

import android.util.Log
import com.example.pruebameli.domain.auth.AuthManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import javax.inject.Inject



class BearerInterceptor @Inject constructor(
    private val authManager: AuthManager
) : Interceptor {
    
    companion object {
        private const val TAG = "BEARER_INTERCEPTOR"
    }
    
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val url = original.url.toString()
        
        Log.d(TAG, "Interceptando request: ${original.method} $url")

        // Si ya viene Authorization, no lo pisamos (evita duplicidad)
        if (original.header("Authorization") != null) {
            Log.d(TAG, "Request ya tiene header Authorization - No se modifica")
            return chain.proceed(original)
        }

        // OkHttp interceptor no es suspend → por eso usamos runBlocking
        // Importante: AuthManager debe refrescar SOLO si toca, no siempre.
        Log.d(TAG, "Obteniendo token válido para request")
        
        val token = try {
            runBlocking { authManager.getValidAccessToken() }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener token: ${e.message}", e)
            null
        }

        val newRequest = if (!token.isNullOrBlank()) {
            Log.d(TAG, "Token agregado al header (${token.take(20)}...)")
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Log.w(TAG, "No hay token disponible - Request sin autenticación")
            //Si no hay token, dejamos el request igual (por si es endpoint público)
            original
        }

        return chain.proceed(newRequest)
    }
}
