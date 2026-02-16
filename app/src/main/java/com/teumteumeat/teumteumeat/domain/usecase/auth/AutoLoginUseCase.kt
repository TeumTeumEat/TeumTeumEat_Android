package com.teumteumeat.teumteumeat.domain.usecase.auth

import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
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

}
