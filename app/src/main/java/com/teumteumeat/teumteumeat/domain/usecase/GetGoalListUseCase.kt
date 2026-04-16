package com.teumteumeat.teumteumeat.domain.usecase

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import javax.inject.Inject

class GetGoalListUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {

    suspend operator fun invoke(): ApiResultV2<GoalsData> {
        return goalRepository.getGoalList()
    }
}
