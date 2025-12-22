package com.teumteumeat.teumteumeat.data.network.model

data class ApiResponse<T>(
    val code: String?,
    val message: String?,
    val details: String?,
    val data: T
)

sealed class ApiResult<out T> {

    data class Success<T>(
        val message: String?,
        val data: T
    ) : ApiResult<T>()

    data class SessionExpired(
        val message: String
    ) : ApiResult<Nothing>()

    data class NetworkError(
        val message: String
    ) : ApiResult<Nothing>()

    data class ServerError(
        val code: String,
        val message: String
    ) : ApiResult<Nothing>()

    data class UnknownError(
        val message: String = "unknown",
        val throwable: Throwable? = null
    ) : ApiResult<Nothing>()
}