package com.teumteumeat.teumteumeat.data.repository.login

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.network.model.AuthToken
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.domain.model.auth.ResponseBody
import com.teumteumeat.teumteumeat.domain.model.auth.SessionResult
import javax.inject.Inject

class SocialLoginRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource
) : SocialLoginRepository {

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
}
