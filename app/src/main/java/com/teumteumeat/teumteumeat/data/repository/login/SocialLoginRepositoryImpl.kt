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
import javax.inject.Inject

class SocialLoginRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource
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

        // 2) 재발급 시도
        val reissueResponse = authApiService.reissueAccessToken(ResponseBody(refreshToken))

        // 3) 재발급 실패 → 세션 종료 처리
        if (reissueResponse.code != "OK" || reissueResponse.data.isBlank()) {
            tokenLocalDataSource.clear()
            return SessionResult.Failed(
                code = reissueResponse.code,
                message = reissueResponse.message ?: "토큰 갱신에 실패했습니다."
            )
        }

        // 4) 재발급 성공 -> 리프래쉬 토큰 저장
        tokenLocalDataSource.save(
            AuthToken(
                accessToken = reissueResponse.data,
                refreshToken = refreshToken
            )
        )

        return SessionResult.Success
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
