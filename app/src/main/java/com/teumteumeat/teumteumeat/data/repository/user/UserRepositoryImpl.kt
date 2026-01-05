package com.teumteumeat.teumteumeat.data.repository.user

import android.util.Log
import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.category.CategoryApiService
import com.teumteumeat.teumteumeat.data.api.user.CommuteTimeRequest
import com.teumteumeat.teumteumeat.data.api.user.UserApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingStatus
import com.teumteumeat.teumteumeat.data.api.user.UpdateNameRequest
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.DomainError
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_response.AccountInfo
import com.teumteumeat.teumteumeat.data.network.model_response.AccountInfoResponse
import com.teumteumeat.teumteumeat.data.network.model_response.toDomain
import com.teumteumeat.teumteumeat.data.network.model_response.user.CommuteInfoResponse
import com.teumteumeat.teumteumeat.domain.model.on_boarding.CategoriesResponseDto
import com.teumteumeat.teumteumeat.domain.model.on_boarding.UserName
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toDomain
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toDomainCategoryTree
import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val categoryApiService: CategoryApiService,
    private val userApi: UserApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource
) : BaseRepository(authApiService, tokenLocalDataSource),
    UserRepository {

    override suspend fun getCommuteInfo(): ApiResultV2<CommuteInfoResponse> {
        return safeApiVer2(
            apiCall = {
                userApi.getCommuteInfo()
            },
            mapper = { data ->
                CommuteInfoResponse(
                    startTime = data?.startTime.orEmpty(),
                    endTime = data?.endTime.orEmpty(),
                    usageTime = data?.usageTime ?: 0
                )
            }
        )
    }

    override suspend fun getUserName(): ApiResultV2<UserName> {
        return safeApiVer2(
            apiCall = { userApi.getUserName() },
            mapper = { dto ->
                // dto는 nullable로 들어오니까 NPE 방지
                UserName(name = dto?.name.orEmpty())
            }
        )
    }

    override suspend fun getAccountInfo(): ApiResultV2<AccountInfoResponse> {
        return safeApiVer2(
            apiCall = {
                userApi.getAccountInfo()
            },
            mapper = { response ->
                response ?: AccountInfoResponse(
                    socialProvider = SocialProvider.NONE,
                    email = "잘못된 로그인"
                )
            }
        )
    }

    override suspend fun getOnboardingStatus(): ApiResult<OnboardingStatus, Unit> {
        return safeApiCall(
            apiCall = { userApi.getOnboardingCompleted() },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun getOnboardingCompletedV2(): ApiResultV2<OnboardingStatus> {
        return safeApiVer2(
            apiCall = {
                userApi.getOnboardingCompletedV2()
            },
            mapper = { it?.toDomain() ?: OnboardingStatus(false) }
        )
    }

    override suspend fun updateUserNameV2(
        name: String
    ): ApiResultV2<String> {


        return safeApiVer2<Any, String>(
            apiCall = {
                userApi.updateUserNameV2(UpdateNameRequest(name))
            },
            mapper = { _ -> "" }
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


    override suspend fun updateCommuteInfo(
        request: CommuteTimeRequest
    ): ApiResultV2<Unit> {

        return safeApiVer2(
            apiCall = {
                userApi.updateCommuteInfo(request)
            },
            mapper = {
                // Unit 응답이므로 그대로 반환
                Unit
            }
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
