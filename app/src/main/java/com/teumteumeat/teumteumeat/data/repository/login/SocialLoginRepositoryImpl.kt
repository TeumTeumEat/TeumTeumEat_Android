package com.teumteumeat.teumteumeat.data.repository.login

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.user.UserApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.AuthToken
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_request.AuthResponse
import com.teumteumeat.teumteumeat.data.network.model_response.SocialLoginRequest
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.auth.ResponseBody
import com.teumteumeat.teumteumeat.domain.model.auth.SessionResult
import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

class SocialLoginRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : BaseRepository(authApiService, tokenLocalDataSource), SocialLoginRepository {


    override suspend fun withdrawUser(): ApiResultV2<Unit> =
        safeApiVer2(
            apiCall = { authApiService.withdrawUser() },
            mapper = {
                // data는 의미 없으므로 버림
                Unit
            }
        )

    override suspend fun validateSession(): SessionResult {

        // 1️⃣ refreshToken 확인
        val refreshToken = tokenLocalDataSource.getRefreshToken()
            ?: return SessionResult.Failed(message = "로그인이 필요합니다.")

        return try {
            val reissueResponse = authApiService.reissueAccessToken(
                ResponseBody(refreshToken)
            )

            if (reissueResponse.code != "OK" || reissueResponse.data.isBlank()) {
                tokenLocalDataSource.clear()
                SessionResult.Expired(
                    code = reissueResponse.code,
                    message = reissueResponse.message ?: "토큰 갱신 실패"
                )
            } else {
                tokenLocalDataSource.save(
                    AuthToken(
                        accessToken = reissueResponse.data,
                        refreshToken = refreshToken
                    )
                )
                SessionResult.Success
            }

        } catch (e: UnknownHostException) {
            SessionResult.NetworkError("인터넷 연결이 필요합니다.")
        } catch (e: IOException) {
            SessionResult.NetworkError("네트워크 연결을 확인해주세요.")
        }
    }


    override suspend fun socialLogin(
        provider: String,
        request: SocialLoginRequest,
    ): ApiResultV2<AuthResponse?> {

        return safeApiVer2(
            apiCall = {
                authApiService.socialLogin(
                    provider = provider,
                    request = request,
                )
            },
            mapper = {
                it
            }
        )
    }


}
