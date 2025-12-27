package com.teumteumeat.teumteumeat.domain.usecase.on_boarding

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import javax.inject.Inject

class CreateGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(
        request: CreateGoalRequest
    ): ApiResultV2<Unit> {
        return goalRepository.createGoal(request)
    }
}
