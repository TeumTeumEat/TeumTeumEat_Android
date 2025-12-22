package com.teumteumeat.teumteumeat.data.repository.user

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.on_boarding.UserApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.AuthToken
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.user.OnboardingStatus
import com.teumteumeat.teumteumeat.domain.model.user.toDomain
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource
) : BaseRepository(authApiService, tokenLocalDataSource),
    UserRepository {

    override suspend fun getOnboardingStatus(): ApiResult<OnboardingStatus> {
        return safeApiCall(
            apiCall = { userApi.getOnboardingCompleted() },
            mapper = { it.toDomain() }
        )
    }
}
