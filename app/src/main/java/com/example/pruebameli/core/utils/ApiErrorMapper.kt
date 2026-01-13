package com.example.pruebameli.core.utils

import com.example.pruebameli.domain.common.ResourceData
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ApiErrorMapper {

    fun fromHttp(code: Int, rawMessage: String? = null): ResourceData.Error {
        val msg = when (code) {
            400 -> "Solicitud inválida."
            401 -> "Sesión expirada o no autorizada."
            403 -> "No tienes permisos para ver este contenido."
            404 -> "No encontramos el producto."
            408 -> "Tiempo de espera agotado. Intenta de nuevo."
            in 500..599 -> "Servidor no disponible. Intenta más tarde."
            else -> "Error HTTP $code${rawMessage?.let { ": $it" } ?: ""}"
        }
        return ResourceData.Error(message = msg, code = code)
    }

    fun fromThrowable(t: Throwable): ResourceData.Error {
        // no convertir cancelación en error
        if (t is CancellationException) throw t

        val msg = when (t) {
            is UnknownHostException -> "Sin conexión. Verifica tu internet."
            is SocketTimeoutException -> "La solicitud tardó demasiado. Intenta de nuevo."
            is IOException -> "Error de conexión. Intenta de nuevo."
            is HttpException -> {
                val code = t.code()
                return fromHttp(code, t.message())
            }

            else -> "Ocurrió un error inesperado."
        }

        return ResourceData.Error(message = msg, cause = t)
    }
}
