package com.teumteumeat.teumteumeat.data.repository.goal

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.goal.GoalApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_request.UpdateGoalRequest
import com.teumteumeat.teumteumeat.data.network.model_response.CreateGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.data.network.model_response.GoalsData
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.mapper.goal.toDomain
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.utils.Utils.RepositoryUtils.requireNotNullOrError
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalApiService: GoalApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : BaseRepository(authApiService, tokenLocalDataSource), GoalRepository {

    /**
     * 목표 수정 요청
     */
    override suspend fun updateGoal(
        goalId: Long,
    ): ApiResultV2<Unit> {
        return safeApiVer2(
            apiCall = {
                goalApiService.updateGoal(
                    goalId = goalId,
                )
            },
            mapper = {
                Unit
            }
        )
    }

    override suspend fun getUserGoal(): ApiResultV2<UserGoal> {

        val url = "/api/v1/users/goal"

        return safeApiVer2(
            apiCall = {
                goalApiService.getUserGoal()
            },
            mapper = { response ->
                // ✅ data 는 null 이면 안되는 API
                // → mapper 단계에서 바로 Domain 변환
                response
                    .requireNotNullOrError(url)
                    .toDomain()
            }
        )
    }

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
