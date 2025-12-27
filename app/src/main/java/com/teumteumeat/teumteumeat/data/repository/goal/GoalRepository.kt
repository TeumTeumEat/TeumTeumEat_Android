package com.teumteumeat.teumteumeat.data.repository.goal

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest

interface GoalRepository {
    suspend fun createGoal(request: CreateGoalRequest): ApiResultV2<Unit>
}
