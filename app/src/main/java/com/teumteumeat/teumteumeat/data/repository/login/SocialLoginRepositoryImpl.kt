package com.teumteumeat.teumteumeat.data.repository.login

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.network.model.AuthToken
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.domain.model.ResponseBody
import com.teumteumeat.teumteumeat.domain.model.SessionResult
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
    /*override suspend fun validateSession(): Boolean {
        *//*//*/ 1️⃣ 우선 "원래 하려던 API" 실행
        // - GET /api/v1/users/id
        // - AuthInterceptor가 있다면 accessToken 자동 첨부
        val response = authApiService.checkMyToken()

        // 2️⃣ 토큰 만료가 아니라면 그대로 반환
        // - OK
        // - 또는 기타 비즈니스 에러
        if (response.code != "AUTH-002") {
            return response
        }*//*

        // 3️⃣ 여기부터는 accessToken 만료 상태

        // 3-
        val refreshToken = tokenLocalDataSource.getRefreshToken()
            ?: return false   // 재발급 불가 → 세션 종료

        // 4️⃣ 재발급 API 호출 (Authorization 헤더 ❌)
        val reissueResponse = authApiService.reissueAccessToken(refreshToken)

        // 5️⃣ 재발급 실패 → 세션 종료 처리
        if (reissueResponse.code != "OK" || reissueResponse.data.isBlank()) {
            tokenLocalDataSource.clear()
            return false
        }

        // 6️⃣ 재발급 성공 → 새 accessToken 저장
        tokenLocalDataSource.save(
            AuthToken(
                accessToken = reissueResponse.data,
                refreshToken = refreshToken
            )
        )

        return true
    }*/