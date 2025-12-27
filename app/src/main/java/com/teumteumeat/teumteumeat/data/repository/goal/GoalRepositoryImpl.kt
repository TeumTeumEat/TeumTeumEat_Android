package com.teumteumeat.teumteumeat.data.repository.goal

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.goal.GoalApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_request.CreateGoalRequest
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalApiService: GoalApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : BaseRepository(authApiService, tokenLocalDataSource), GoalRepository {

    override suspend fun createGoal(
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
