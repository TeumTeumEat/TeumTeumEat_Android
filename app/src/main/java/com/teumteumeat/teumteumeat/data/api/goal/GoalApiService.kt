package com.teumteumeat.teumteumeat.data.api.goal

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.CreateGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GoalApiService {

    @POST("/api/v1/goals")
    suspend fun createGoal(
        @Body request: CreateGoalRequest
    ): ApiResponse<CreateGoalResponse, Any?>

    @POST("/api/v1/goals")
    suspend fun createGoalv1(
        @Body request: CreateGoalRequest
    ): ApiResponse<Unit, Any?>

    @GET("/api/v1/goals")
    suspend fun getGoals(): ApiResponse<GoalsData, Any?>
}