package com.teumteumeat.teumteumeat.data.repository.user

import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.domain.model.user.OnboardingStatus

interface UserRepository {
    suspend fun getOnboardingStatus(): ApiResult<OnboardingStatus>
}