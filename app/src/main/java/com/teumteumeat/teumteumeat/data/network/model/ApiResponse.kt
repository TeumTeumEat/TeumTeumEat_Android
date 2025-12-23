package com.teumteumeat.teumteumeat.data.network.model

data class ApiResponse<T, D>(
    val code: String?,
    val message: String?,
    val details: D,
    val data: T
)

sealed class ApiResult<out T, out D> {

    data class Success<T>(
        val message: String?,
        val data: T
    ) : ApiResult<T, Nothing>()

    data class SessionExpired(
        val message: String
    ) : ApiResult<Nothing, Nothing>()

    data class NetworkError(
        val message: String
    ) : ApiResult<Nothing, Nothing>()

    data class ServerError<D>(
        val code: String,
        val message: String,
        val details: D?
    ) : ApiResult<Nothing, D>()

    data class UnknownError(
        val message: String = "unknown",
        val throwable: Throwable? = null
    ) : ApiResult<Nothing, Nothing>()
}