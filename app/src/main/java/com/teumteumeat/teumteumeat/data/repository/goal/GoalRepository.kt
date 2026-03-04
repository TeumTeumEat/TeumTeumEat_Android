package com.teumteumeat.teumteumeat.data.repository.goal

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_request.UpdateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import kotlinx.coroutines.flow.SharedFlow

interface GoalRepository {

    // 1. 상태 업데이트를 감지할 Flow 정의
    val refreshSignal: SharedFlow<Unit>

    // 시그널을 외부(ViewModel)에서 보낼 수 있도록 함수 추가
    suspend fun emitRefreshSignal()

    /**
     * 목표 수정 요청
     */
    suspend fun updateGoal(goalId: Long): ApiResultV2<Unit>

    /**
     * 현재 유저 목표 조회
     */
    suspend fun getUserGoal(): ApiResultV2<UserGoal>

    suspend fun getGoalList(): ApiResultV2<GoalsData>
    suspend fun createGoal(request: CreateGoalRequest): ApiResultV2<Int>
    suspend fun createGoalV1(request: CreateGoalRequest): ApiResultV2<Unit>
}
