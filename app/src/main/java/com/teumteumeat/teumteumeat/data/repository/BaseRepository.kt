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
        val response = apiCall()
        // 🔍 [디버깅 로그] 실제 서버가 보낸 code와 message 확인
        println("DEBUG: Server Code=[${response.code}], Type=[${response.code?.javaClass?.simpleName}]")
        println("DEBUG: Server Message=[${response.message}]")
        println("DEBUG: isSuccessCode() result=[${response.code.isSuccessCode()}]")

        // 수정된 코드
        return try {
            // return@try 제거함. when의 결과가 자동으로 try의 반환값이 됩니다.
            when {
                // ✅ 성공 응답
                response.code.isSuccessCode() -> {
                    ApiResultV2.Success(
                        message = response.message,
                        data = mapper(response.data)
                    )
                }

                // 🔐 인증 관련 에러 (AUTH-001 ~ AUTH-005)
                response.code.isAuthErrorCode() -> {
                    ApiResultV2.SessionExpired(
                        message = response.message
                            ?: "인증 정보가 유효하지 않습니다. 다시 로그인해주세요."
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
            // ⭐ 핵심: HTTP 에러를 ApiResult로 변환
            handleHttpException(e)

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
                errorResponse.code.isAuthErrorCode() -> {
                    ApiResultV2.SessionExpired(
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

    private fun String?.isAuthErrorCode(): Boolean =
        this?.startsWith("AUTH-") == true

    protected suspend fun <T, R> handleUnauthorizedVer2(
        apiCall: suspend () -> ApiResponse<T, Any?>,
        mapper: (T) -> R
    ): ApiResultV2<R> {

        val refreshToken = tokenLocalDataSource.getRefreshToken()
            ?: return ApiResultV2.SessionExpired(
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

            ApiResultV2.Success(
                message = retryResponse.message,
                data = mapper(retryResponse.data)
            )

        } catch (e: Exception) {
            tokenLocalDataSource.clear()
            ApiResultV2.SessionExpired(
                message = "로그인이 만료되었습니다. 다시 로그인해주세요."
            )
        }
    }


    protected suspend fun <T, R, D> safeApiCall(
        apiCall: suspend () -> ApiResponse<T, D>,
        mapper: (T) -> R
    ): ApiResult<R, D>{

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
            ApiResult.NetworkError(
                message = "네트워크 연결을 확인해주세요."
            )

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
    ): ApiResult<R, D>
    {

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
