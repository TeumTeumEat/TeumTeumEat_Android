package com.teumteumeat.teumteumeat.data.api.goal

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_request.UpdateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.CreateGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData
import com.teumteumeat.teumteumeat.data.network.model_response.goal.UserGoalResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Query

interface GoalApiService {

    /**
     * 목표 수정 API
     *
     * @param goalId 수정할 목표의 고유 ID (Query Parameter)
     * @param request 목표 수정에 필요한 데이터 (생성과 동일한 Body)
     * @return 수정된 목표 정보
     */
    @PATCH("/api/v1/goals")
    suspend fun updateGoal(
        @Query("goalId") goalId: Long,
        @Body request: UpdateGoalRequest
    ): ApiResponse<Unit, Any?>

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