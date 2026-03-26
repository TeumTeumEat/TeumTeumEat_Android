package com.teumteumeat.teumteumeat.data.network.model

import com.teumteumeat.teumteumeat.data.repository.BaseRepository

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

// 어디든 접근 가능한 파일 (ex: ApiResultExt.kt)
val ApiResult<*, *>.uiMessage: String
    get() = when (this) {
        is ApiResult.Success -> this.message ?: ""
        is ApiResult.ServerError -> this.message
        is ApiResult.SessionExpired -> this.message
        is ApiResult.NetworkError -> this.message
        is ApiResult.UnknownError -> this.message
    }

// 2️⃣ ApiResultV2 정의: 제네릭 D를 제거하고 DomainError를 내장
sealed class ApiResultV2<out T> {

    data class Success<T>(
        val message: String?,
        val data: T
    ) : ApiResultV2<T>()

    data class SessionExpired(
        val code: BaseRepository.SessionErrorCode,
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

// ApiResultV2.kt 근처 또는 Utils 파일
fun <T, R> ApiResultV2<T>.map(transform: (T) -> R): ApiResultV2<R> {
    return when (this) {
        is ApiResultV2.Success -> ApiResultV2.Success(
            message = this.message, // 성공 메시지는 그대로 유지
            data = transform(this.data)
        )

        is ApiResultV2.SessionExpired -> ApiResultV2.SessionExpired(
            code = this.code,
            message = this.uiMessage // 확장 프로퍼티 uiMessage의 로직 결과(RETRY/FAIL 처리된 문자열)를 할당
        )

        is ApiResultV2.NetworkError -> ApiResultV2.NetworkError(
            message = this.uiMessage // "네트워크 에러" 메시지 유지
        )

        is ApiResultV2.ServerError -> ApiResultV2.ServerError(
            code = this.code,
            message = this.uiMessage, // DomainError(Validation, Message 등)가 처리된 최종 문자열을 할당
            errorType = this.errorType
        )

        is ApiResultV2.UnknownError -> ApiResultV2.UnknownError(
            message = this.uiMessage,
            throwable = this.throwable
        )
    }
}

val ApiResultV2<*>.uiMessage: String
    get() = when (this) {

        is ApiResultV2.Success ->
            this.message ?: ""

        is ApiResultV2.ServerError -> {
            when (val error = this.errorType) {

                is DomainError.Validation ->
                    error.errors.joinToString("\n") { it.message }

                is DomainError.Message ->
                    error.message

                DomainError.None ->
                    this.message
            }
        }

        is ApiResultV2.SessionExpired -> {
            when (this.code) {
                BaseRepository.SessionErrorCode.RETRY ->
                    "인증이 만료되어 다시 시도합니다."

                BaseRepository.SessionErrorCode.FAIL ->
                    this.message.ifBlank {
                        "로그인이 만료되었습니다. 다시 로그인해주세요."
                    }
            }
        }

        is ApiResultV2.NetworkError ->
            this.message

        is ApiResultV2.UnknownError ->
            this.message
    }



