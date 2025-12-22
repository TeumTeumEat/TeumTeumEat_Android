package com.teumteumeat.teumteumeat.domain.model.auth

sealed class SessionResult {
    object Success : SessionResult()
    data class Failed(
        val code: String? = null,
        val message: String?,
    ) : SessionResult()
}

data class ResponseBody(
    val refreshToken: String?,
)