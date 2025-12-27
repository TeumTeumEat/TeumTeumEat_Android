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
        mapper: (T) -> R
    ): ApiResult<R, DomainError?> {

        return try {
            val response = apiCall()

            when (response.code) {

                "OK", "CREATED" -> {
                    ApiResult.Success(
                        message = response.message,
                        data = mapper(response.data)
                    )
                }

                "AUTH-002" -> {
                    ApiResult.SessionExpired(
                        message = response.message ?: "토큰이 만료되었습니다."
                    )
                }

                else -> {
                    ApiResult.ServerError(
                        code = response.code ?: "UNKNOWN",
                        message = response.message ?: "서버 오류가 발생했습니다.",
                        details = response.details?.toDomainError()
                    )
                }
            }

        } catch (e: UnauthorizedException) {
            handleUnauthorizedVer2(apiCall, mapper)

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


    protected suspend fun <T, R> handleUnauthorizedVer2(
        apiCall: suspend () -> ApiResponse<T, Any?>,
        mapper: (T) -> R
    ): ApiResult<R, DomainError?> {

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
                message = retryResponse.message,
                data = mapper(retryResponse.data)
            )

        } catch (e: Exception) {
            tokenLocalDataSource.clear()
            ApiResult.SessionExpired(
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
