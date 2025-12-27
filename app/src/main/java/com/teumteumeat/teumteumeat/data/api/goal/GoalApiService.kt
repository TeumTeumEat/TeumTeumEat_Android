package com.teumteumeat.teumteumeat.data.api.goal

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface GoalApiService {

    @POST("/api/v1/goals")
    suspend fun createGoal(
        @Body request: CreateGoalRequest
    ): ApiResponse<Unit?, Any?>
}