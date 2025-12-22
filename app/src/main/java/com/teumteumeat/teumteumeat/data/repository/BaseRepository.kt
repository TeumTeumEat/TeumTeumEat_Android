package com.teumteumeat.teumteumeat.data.repository

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.network.exception.UnauthorizedException
import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.AuthToken
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.domain.model.auth.ResponseBody
import java.io.IOException

abstract class BaseRepository(
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource
) {

    protected suspend fun <T, R> safeApiCall(
        apiCall: suspend () -> ApiResponse<T>,
        mapper: (T) -> R
    ): ApiResult<R> {

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
                        message = response.message ?: "서버 오류가 발생했습니다."
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

    private suspend fun <T, R> handleUnauthorized(
        apiCall: suspend () -> ApiResponse<T>,
        mapper: (T) -> R
    ): ApiResult<R> {

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
