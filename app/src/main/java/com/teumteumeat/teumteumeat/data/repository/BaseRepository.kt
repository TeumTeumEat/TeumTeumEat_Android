package com.teumteumeat.teumteumeat.data.repository

import com.google.gson.Gson
import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.network.exception.UnauthorizedException
import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.AuthToken
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.domain.model.auth.ResponseBody
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseRepository(
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource
) {

    private fun Any?.toDomainError(): DomainError {
        return when (this) {
            null -> DomainError.None

            is String ->
                DomainError.Message(this)

            is List<*> ->
                DomainError.Validation(
                    this.filterIsInstance<FieldErrorDetail>()
                )

            else ->
                DomainError.Message("알 수 없는 오류 형식입니다.")
        }
    }

    protected suspend fun <T, R> safeApiVer2(
        apiCall: suspend () -> ApiResponse<T, Any?>,
        mapper: (T?) -> R
    ): ApiResultV2<R> {
        // 수정된 코드
        return try {
            val response = apiCall()
            // 🔍 [디버깅 로그] 실제 서버가 보낸 code와 message 확인
            println("DEBUG: Server Code=[${response.code}], Type=[${response.code?.javaClass?.simpleName}]")
            println("DEBUG: Server Message=[${response.message}]")
            println("DEBUG: isSuccessCode() result=[${response.code.isSuccessCode()}]")

            // return@try 제거함. when의 결과가 자동으로 try의 반환값이 됩니다.
            when {
                // ✅ 성공 응답
                response.code.isSuccessCode() -> {
                    ApiResultV2.Success(
                        message = response.message,
                        data = mapper(response.data)
                    )
                }

                // ❗ refresh 대상이 아님 (비즈니스 인증 에러)
                response.code == "AUTH-006" -> {
                    ApiResultV2.ServerError(
                        code = response.code,
                        message = response.message ?: "",
                        errorType = DomainError.None,
                    )
                }

                // ❌ 그 외 모든 서버 에러
                else -> {
                    val domainError = when {
                        // 1️⃣ details가 있는 경우 (검증 에러 등)
                        response.details != null -> {
                            response.details.toDomainError()
                        }

                        // 2️⃣ details는 없고 message만 있는 경우
                        !response.message.isNullOrBlank() -> {
                            DomainError.Message(response.message)
                        }

                        // 3️⃣ 둘 다 없는 경우
                        else -> {
                            DomainError.None
                        }
                    }

                    ApiResultV2.ServerError(
                        code = response.code ?: "UNKNOWN",
                        message = response.message ?: "서버 오류가 발생했습니다.",
                        errorType = domainError
                    )
                }
            }

        } catch (e: retrofit2.HttpException) {
            // 🔥 서버 에러 body 파싱
            val errorResponse = parseErrorResponse(e)

            // ✅ 서버 code가 있으면 → 무조건 그 code 사용
            if (errorResponse?.code != null) {
                return ApiResultV2.ServerError(
                    code = errorResponse.code,
                    message = errorResponse.message ?: "서버 오류가 발생했습니다.",
                    errorType = DomainError.None
                )
            }

            // ✅ 오직 HTTP 401만 refresh 시도
            if (e.code() == 401) {
                handleUnauthorizedVer2(apiCall, mapper)
            } else {
                handleHttpException(e)
            }
        } catch (e: UnauthorizedException) {
            handleUnauthorizedVer2(apiCall, mapper)

        } catch (e: IOException) {
            ApiResultV2.NetworkError(
                message = "네트워크 연결을 확인해주세요."
            )

        } catch (e: Exception) {
            // ⭐ 여기가 범인인지 확인하는 로그
            println("DEBUG: Exception caught! message=[${e.message}]")
            e.printStackTrace() // 스택 트레이스를 보면 mapper 어디서 터졌는지 알 수 있음

            ApiResultV2.UnknownError(
                message = e.message.toString(),
                throwable = e
            )

        }
    }
    private fun parseErrorResponse(
        e: retrofit2.HttpException
    ): ApiResponse<Nothing, Any?>? {

        return try {
            // 1️⃣ errorBody 문자열 추출
            val errorBody = e.response()
                ?.errorBody()
                ?.string()
                ?.takeIf { it.isNotBlank() }
                ?: return null

            // 2️⃣ JSON → ApiResponse 파싱
            Gson().fromJson(
                errorBody,
                object : com.google.gson.reflect.TypeToken<
                        ApiResponse<Nothing, Any?>
                        >() {}.type
            )
        } catch (ex: Exception) {
            // 3️⃣ 파싱 실패 → 서버 code 없음으로 간주
            null
        }
    }


    private fun handleHttpException(
        e: retrofit2.HttpException
    ): ApiResultV2<Nothing> {

        return try {
            val errorJson = e.response()?.errorBody()?.string()

            if (errorJson.isNullOrBlank()) {
                return ApiResultV2.ServerError(
                    code = e.code().toString(),
                    message = "서버 오류가 발생했습니다.",
                    errorType = DomainError.None
                )
            }

            val errorResponse =
                Gson().fromJson(errorJson, ApiResponse::class.java)

            when {
                // 인증 관련 에러 처리
                errorResponse.code.isAuthErrorCode() -> {
                    ApiResultV2.SessionExpired(
                        code = SessionErrorCode.FAIL,
                        message = errorResponse.message
                            ?: "인증 정보가 유효하지 않습니다."
                    )
                }

                else -> {
                    ApiResultV2.ServerError(
                        code = errorResponse.code ?: "UNKNOWN",
                        message = errorResponse.message ?: "서버 오류가 발생했습니다.",
                        errorType = errorResponse.details?.toDomainError()
                            ?: DomainError.None
                    )
                }
            }

        } catch (ex: Exception) {
            ApiResultV2.UnknownError(
                message = "서버 오류가 발생했습니다.",
                throwable = ex
            )
        }
    }

    private fun String?.isSuccessCode(): Boolean {
        if (this == null) return false
        // 공백 제거 후 대소문자 무시하고 비교
        val code = this.trim().uppercase()
        return code == "OK" || code == "CREATED" || code == "SUCCESS" || code == "200"
    }

    private fun String?.isAuthErrorCode(): Boolean {
        return this in listOf(
            "AUTH-001",
            "AUTH-002",
            "AUTH-003",
            "AUTH-004",
            "AUTH-005"
            // ❌ AUTH-006 제거
        )
    }

    enum class SessionErrorCode { RETRY, FAIL }

    protected suspend fun <T, R> handleUnauthorizedVer2(
        apiCall: suspend () -> ApiResponse<T, Any?>,
        mapper: (T) -> R
    ): ApiResultV2<R> {

        val refreshToken = tokenLocalDataSource.getRefreshToken()
            ?: return ApiResultV2.SessionExpired(
                code = SessionErrorCode.RETRY,
                message = "로그인이 만료되었습니다. 다시 로그인해주세요."
            )

        return try {
            val tokenResponse =
                authApiService.reissueAccessToken(ResponseBody(refreshToken))

            tokenLocalDataSource.save(
                AuthToken(
                    accessToken = tokenResponse.data,
                    refreshToken = refreshToken,
                )
            )

            val retryResponse = apiCall()

            ApiResultV2.Success(
                message = retryResponse.message,
                data = mapper(retryResponse.data)
            )

        } catch (e: Exception) {
            tokenLocalDataSource.clear()
            ApiResultV2.SessionExpired(
                code = SessionErrorCode.FAIL,
                message = "로그인이 만료되었습니다. 다시 로그인해주세요."
            )
        }
    }


    protected suspend fun <T, R, D> safeApiCall(
        apiCall: suspend () -> ApiResponse<T, D>,
        mapper: (T) -> R
    ): ApiResult<R, D> {

        return try {
            val response = apiCall()

            when (response.code) {
                "OK" -> {
                    ApiResult.Success(
                        data = mapper(response.data),
                        message = response.message
                    )
                }

                "AUTH-002" -> {
                    ApiResult.SessionExpired(
                        message = response.message ?: "세션이 만료되었습니다."
                    )
                }

                else -> {
                    ApiResult.ServerError(
                        code = response.code ?: "UNKNOWN",
                        message = response.message ?: "서버 오류가 발생했습니다.",
                        details = response.details
                    )
                }
            }

        } catch (e: UnauthorizedException) {
            handleUnauthorized(apiCall, mapper)

        } catch (e: IOException) {
            val message = when (e) {
                is SocketTimeoutException ->
                    "요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."

                is UnknownHostException ->
                    "인터넷 연결이 없습니다. 네트워크 상태를 확인해주세요."

                is ConnectException ->
                    "서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요."

                else ->
                    "네트워크 오류가 발생했습니다. 연결 상태를 확인해주세요."
            }

            ApiResult.NetworkError(message = message)

        } catch (e: Exception) {
            ApiResult.UnknownError(
                message = "알 수 없는 오류가 발생했습니다.",
                throwable = e
            )
        }
    }

    protected suspend fun <T, R, D> handleUnauthorized(
        apiCall: suspend () -> ApiResponse<T, D>,
        mapper: (T) -> R
    ): ApiResult<R, D> {

        val refreshToken = tokenLocalDataSource.getRefreshToken()
            ?: return ApiResult.SessionExpired(
                message = "로그인이 만료되었습니다. 다시 로그인해주세요."
            )

        return try {
            val tokenResponse =
                authApiService.reissueAccessToken(ResponseBody(refreshToken))

            tokenLocalDataSource.save(
                AuthToken(
                    accessToken = tokenResponse.data,
                    refreshToken = refreshToken
                )
            )

            val retryResponse = apiCall()

            ApiResult.Success(
                data = mapper(retryResponse.data),
                message = retryResponse.message
            )

        } catch (e: Exception) {
            tokenLocalDataSource.clear()
            ApiResult.SessionExpired(
                message = "로그인이 만료되었습니다. 다시 로그인해주세요."
            )
        }
    }
}
