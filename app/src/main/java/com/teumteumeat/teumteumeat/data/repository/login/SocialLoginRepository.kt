package com.teumteumeat.teumteumeat.data.repository.login

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_request.AuthResponse
import com.teumteumeat.teumteumeat.data.network.model_response.SocialLoginRequest
import com.teumteumeat.teumteumeat.domain.model.auth.SessionResult
import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider

interface SocialLoginRepository {
    suspend fun validateSession(): SessionResult
    suspend fun socialLogin(
        provider: String,
        request: SocialLoginRequest,
    ): ApiResultV2<AuthResponse?>
}