package com.teumteumeat.teumteumeat.domain.usecase

import com.teumteumeat.teumteumeat.data.repository.login.AutoLogin
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.domain.model.auth.SessionResult
import javax.inject.Inject

class AutoLoginUseCase @Inject constructor(
    private val repository: SocialLoginRepository
) {
    suspend operator fun invoke(): AutoLogin {

        // Repository에서 "리프레쉬 토큰의 유효함"의 결과를 줌
        val response = repository.validateSession()

        return when (response) {
            is SessionResult.Success  -> {
                // 최종적으로 인증 성공
                AutoLogin.Success
            }

            is SessionResult.Failed -> {
                AutoLogin.Fail(response.message?:"Unknown Error: 메시지 없음")
            }

        }
    }
}
