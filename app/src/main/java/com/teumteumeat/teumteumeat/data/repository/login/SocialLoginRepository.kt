package com.teumteumeat.teumteumeat.data.repository.login

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_request.AuthResponse
import com.teumteumeat.teumteumeat.data.network.model_response.SocialLoginRequest
import com.teumteumeat.teumteumeat.domain.model.auth.SessionResult

interface SocialLoginRepository {
    suspend fun validateSession(): SessionResult

    suspend fun socialLogin(
        provider: String,
        request: SocialLoginRequest
    ): ApiResultV2<AuthResponse?>

    suspend fun withdrawUser(): ApiResultV2<Unit>

    suspend fun logout(
        refreshToken: String,
    ): ApiResultV2<Unit>
}