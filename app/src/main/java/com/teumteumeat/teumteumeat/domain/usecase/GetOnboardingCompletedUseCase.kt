package com.teumteumeat.teumteumeat.domain.usecase

import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.model.user.OnboardingStatus
import javax.inject.Inject

class GetOnboardingCompletedUseCase@Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): ApiResult<OnboardingStatus> {
        return userRepository.getOnboardingStatus()
    }
}
