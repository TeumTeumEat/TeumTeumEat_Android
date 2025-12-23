package com.teumteumeat.teumteumeat.data.repository.user

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.user.CommuteTimeRequest
import com.teumteumeat.teumteumeat.data.api.user.UserApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingStatus
import com.teumteumeat.teumteumeat.data.api.user.UpdateNameRequest
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toDomain
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource
) : BaseRepository(authApiService, tokenLocalDataSource),
    UserRepository {

    override suspend fun getOnboardingStatus(): ApiResult<OnboardingStatus, Unit> {
        return safeApiCall(
            apiCall = { userApi.getOnboardingCompleted() },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun updateUserName(
        name: String
    ): ApiResult<String, List<FieldErrorDetail>> {

        return safeApiCall(
            apiCall = {
                userApi.updateUserName(
                    UpdateNameRequest(name)
                )
            },
            mapper = { name }
        )
    }

    override suspend fun updateCommuteTime(
        request: CommuteTimeRequest
    ): ApiResult<Unit, Unit> {

        return safeApiCall(
            apiCall = {
                userApi.updateCommuteTime(request)
            },
            mapper = {
                // data는 {} 이므로 의미 없음
                Unit
            }
        )
    }

}
