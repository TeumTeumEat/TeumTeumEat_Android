package com.teumteumeat.teumteumeat.data.network.model

data class ApiResponse<T, D>(
    val code: String?,
    val message: String?,
    val details: D?,
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

// 2️⃣ ApiResultV2 정의: 제네릭 D를 제거하고 DomainError를 내장
sealed class ApiResultV2<out T> {

    data class Success<T>(
        val message: String?,
        val data: T
    ) : ApiResultV2<T>()

    data class SessionExpired(
        val message: String
    ) : ApiResultV2<Nothing>()

    data class NetworkError(
        val message: String
    ) : ApiResultV2<Nothing>()

    /**
     * 핵심 변경점:
     * 기존 ServerError<D> 대신 errorType: DomainError를 가짐.
     * 이제 ViewModel은 D가 뭔지 몰라도 됨.
     */
    data class ServerError(
        val code: String,
        val message: String,
        val errorType: DomainError
    ) : ApiResultV2<Nothing>()

    data class UnknownError(
        val message: String = "unknown",
        val throwable: Throwable? = null
    ) : ApiResultV2<Nothing>()
}

