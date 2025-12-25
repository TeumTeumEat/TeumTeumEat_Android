package com.teumteumeat.teumteumeat.data.repository.user

import com.teumteumeat.teumteumeat.data.api.user.CommuteTimeRequest
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingStatus
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category

interface UserRepository {
    suspend fun getOnboardingStatus(): ApiResult<OnboardingStatus, Unit>
    suspend fun updateUserName(name: String): ApiResult<String, List<FieldErrorDetail>>
    suspend fun updateCommuteTime(request: CommuteTimeRequest): ApiResult<Unit, Unit>
    suspend fun getCategories(): ApiResult<List<Category>, Any?>
}