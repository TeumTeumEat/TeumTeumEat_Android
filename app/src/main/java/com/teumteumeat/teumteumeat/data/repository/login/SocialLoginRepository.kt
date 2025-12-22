package com.teumteumeat.teumteumeat.data.repository.login

import com.teumteumeat.teumteumeat.domain.model.auth.SessionResult

interface SocialLoginRepository {
    suspend fun validateSession(): SessionResult
}