package com.example.pruebameli.domain.common


sealed class ResourceData<out T> {
    data class Success<out T>(val data: T) : ResourceData<T>()

    data class Error(
        val message: String,
        val code: Int? = null,
        val cause: Throwable? = null
    ) : ResourceData<Nothing>()

    object Loading : ResourceData<Nothing>()
}
