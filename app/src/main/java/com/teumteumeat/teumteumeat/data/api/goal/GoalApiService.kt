package com.teumteumeat.teumteumeat.data.api.goal

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.CreateGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData
import com.teumteumeat.teumteumeat.data.network.model_response.goal.UserGoalResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GoalApiService {
    /**
     * 현재 로그인 유저의 목표 조회
     * GET /api/v1/users/goal
     */
    @GET("/api/v1/users/goal")
    suspend fun getUserGoal(): ApiResponse<UserGoalResponse, Any?>

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