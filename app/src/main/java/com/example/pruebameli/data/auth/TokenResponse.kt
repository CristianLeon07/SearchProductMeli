package com.example.pruebameli.data.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token")val access_token: String,
    @Json(name = "token_type") val token_type: String,
    @Json(name = "expires_in")val expires_in: Long,
    @Json(name = "scope")val scope: String? = null,
    @Json(name = "user_id") val user_id: Long? = null,
    @Json(name = "refresh_token") val refresh_token: String? = null
)
