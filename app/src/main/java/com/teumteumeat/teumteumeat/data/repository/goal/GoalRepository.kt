package com.teumteumeat.teumteumeat.data.repository.goal

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData

interface GoalRepository {
    suspend fun getGoalList(): ApiResultV2<GoalsData>
    suspend fun createGoal(request: CreateGoalRequest): ApiResultV2<Int>
    suspend fun createGoalV1(request: CreateGoalRequest): ApiResultV2<Unit>
}
