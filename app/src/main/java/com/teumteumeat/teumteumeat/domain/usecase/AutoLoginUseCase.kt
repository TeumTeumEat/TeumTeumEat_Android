package com.teumteumeat.teumteumeat.domain.usecase

import android.util.Log
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_response.SocialLoginRequest
import com.teumteumeat.teumteumeat.data.repository.login.AutoLogin
import com.teumteumeat.teumteumeat.data.repository.login.AutoLogin.*
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.domain.model.auth.SessionResult
import javax.inject.Inject

class AutoLoginUseCase @Inject constructor(
    private val socialLoginRepository: SocialLoginRepository,
    private val tokenLocalDataSource: TokenLocalDataSource,
) {
    suspend operator fun invoke(): AutoLogin {

        return when (val result = socialLoginRepository.validateSession()) {

            is SessionResult.Success -> {
                Success
            }

            is SessionResult.Expired -> {
                SessionExpired(
                    result.message ?: "로그인이 필요합니다."
                )
            }

            is SessionResult.Failed -> {
                Fail(
                    result.message ?: "기타 오류"
                )
            }

            is SessionResult.NetworkError -> {
                NetWorkError(
                    result.message ?: "네트워크 에러"
                )
            }
        }
    }
    /*suspend operator fun invoke(): AutoLogin {
        Log.d("AutoLogin", "invoke() start")
        // 1️⃣ 저장된 소셜 로그인 정보 확인
        val provider = tokenLocalDataSource.getProvider()
        val idToken = tokenLocalDataSource.getIdToken()


        if (provider.isNullOrBlank() || idToken.isNullOrBlank()) {
            return AutoLogin.SessionExpired(
                message = "로그인 정보가 없습니다. 다시 로그인해주세요."
            )
        }

        // 2️⃣ 자동 로그인 요청
        val result = socialLoginRepository.socialLogin(
            provider = provider,
            request = SocialLoginRequest(
                idToken = idToken,
                termsAgreed = true
            )
        )

        // 3️⃣ ApiResultV2 → AutoLogin 변환
        return when (result) {

            is ApiResultV2.Success -> {
                AutoLogin.Success
            }

            is ApiResultV2.SessionExpired -> {
                // 🔑 인증 만료 → 토큰 정리 후 로그인 유도
                tokenLocalDataSource.clear()

                AutoLogin.SessionExpired(
                    message = result.uiMessage
                )
            }

            is ApiResultV2.NetworkError,
            is ApiResultV2.ServerError,
            is ApiResultV2.UnknownError -> {
                AutoLogin.Fail(
                    message = result.uiMessage
                )
            }
        }
    }*/
}
