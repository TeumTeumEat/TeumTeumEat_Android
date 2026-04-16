package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import android.util.Log
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingDecision
import javax.inject.Inject

class GetOnboardingCompletedUseCase@Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): OnboardingDecision {
        return when (val result = userRepository.getOnboardingCompletedV2()) {

            is ApiResultV2.Success -> {
                Log.d("GetOnboardingCompletedUseCase:", "onboarding completed: ${result.data.completed}")
                if (result.data.completed) {
                    Log.d("GoMain:", "onboarding completed: ${result.data}")
                    OnboardingDecision.GoMain
                } else {
                    Log.d("GoOnboarding:", "onboarding completed: ${result.data}")
                    OnboardingDecision.GoOnboarding
                }
            }

            is ApiResultV2.SessionExpired -> {
                OnboardingDecision.NeedLogin(result.uiMessage)
            }

            is ApiResultV2.ServerError,
            is ApiResultV2.NetworkError,
            is ApiResultV2.UnknownError -> {
                OnboardingDecision.NeedLogin(result.uiMessage)
            }
        }
    }
}