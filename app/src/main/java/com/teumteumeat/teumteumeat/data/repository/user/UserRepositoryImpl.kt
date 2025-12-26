package com.teumteumeat.teumteumeat.data.repository.user

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.user.CategoryApiService
import com.teumteumeat.teumteumeat.data.api.user.CommuteTimeRequest
import com.teumteumeat.teumteumeat.data.api.user.UserApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingStatus
import com.teumteumeat.teumteumeat.data.api.user.UpdateNameRequest
import com.teumteumeat.teumteumeat.domain.model.on_boarding.CategoriesResponseDto
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toDomain
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toDomainCategoryTree
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val categoryApiService: CategoryApiService,
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

        return when (
            val result = safeApiCall(
                apiCall = {
                    userApi.updateUserName(UpdateNameRequest(name))
                },
                mapper = { name }
            )
        ) {
            is ApiResult.ServerError -> {
                val details = when (val d = result.details) {
                    is List<*> -> d.filterIsInstance<FieldErrorDetail>()
                    else -> emptyList()
                }

                ApiResult.ServerError(
                    code = result.code,
                    message = result.message,
                    details = details
                )
            }

            else -> result
        }
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

    override suspend fun getCategories(): ApiResult<List<Category>, Any?> {
        return safeApiCall(
            apiCall = {
                categoryApiService.getCategories()
            },
            mapper = { data: CategoriesResponseDto ->
                // 🔽 DTO → 트리 도메인 변환
                data.categoryResponses.toDomainCategoryTree()
            }
        )
    }

}
