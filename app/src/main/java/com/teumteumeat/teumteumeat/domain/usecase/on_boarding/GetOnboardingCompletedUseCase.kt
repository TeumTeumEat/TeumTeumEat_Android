package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingStatus
import javax.inject.Inject

class GetOnboardingCompletedUseCase@Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): ApiResult<OnboardingStatus, Unit> {
        return userRepository.getOnboardingStatus()
    }
}