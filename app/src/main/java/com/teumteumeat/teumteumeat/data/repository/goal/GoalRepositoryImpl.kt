package com.teumteumeat.teumteumeat.data.repository.goal

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.goal.GoalApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.CreateGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalApiService: GoalApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : BaseRepository(authApiService, tokenLocalDataSource), GoalRepository {

    override suspend fun getGoalList(): ApiResultV2<GoalsData> {
        return safeApiVer2(
            apiCall = {
                goalApiService.getGoals()
            },
            mapper = { data ->
                // ⭐ 핵심: goalResponses만 꺼내서 반환
                data ?: error("GoalsData null입니다.")
            }
        )
    }

    override suspend fun createGoal(
        request: CreateGoalRequest
    ): ApiResultV2<Int> {

        return safeApiVer2(
            apiCall = {
                goalApiService.createGoal(request)
            },
            mapper = { data ->
                data?.goalId
                    ?: throw IllegalStateException("goalId가 null입니다.")
            }
        )
    }

    override suspend fun createGoalV1(
        request: CreateGoalRequest
    ): ApiResultV2<Unit> {

        return safeApiVer2(
            apiCall = {
                goalApiService.createGoal(request)
            },
            mapper = {
                Unit
            }
        )
    }
}
