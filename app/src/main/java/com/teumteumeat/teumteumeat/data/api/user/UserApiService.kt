package com.teumteumeat.teumteumeat.data.api.user

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.EmptyResponse
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.data.network.model_request.user.GetUserSettingRequest
import com.teumteumeat.teumteumeat.data.network.model_request.user.UpdateUserSettingRequest
import com.teumteumeat.teumteumeat.data.network.model_response.AccountInfoResponse
import com.teumteumeat.teumteumeat.data.network.model_response.user.CommuteInfoResponse
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingStatus
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import com.teumteumeat.teumteumeat.data.network.model_response.user.UserNameResponseDto

interface UserApiService {

    @PATCH("/api/v1/users/settings")
    suspend fun updateUserSettings(
        @Body request: UpdateUserSettingRequest
    ): ApiResponse<Unit, Any?>

    @GET("/api/v1/users/settings")
    suspend fun getUserSettings():
            ApiResponse<GetUserSettingRequest, Any?>

    @GET("/api/v1/users/commute-info")
    suspend fun getCommuteInfo():
            ApiResponse<CommuteInfoResponse, Any?>

    @GET("/api/v1/users/name")
    suspend fun getUserName(): ApiResponse<UserNameResponseDto, Any?>

    @GET("/api/v1/users/account-info")
    suspend fun getAccountInfo(): ApiResponse<AccountInfoResponse, Any?>

    @GET("/api/v1/users/onboarding-completed")
    suspend fun getOnboardingCompleted(): ApiResponse<OnboardingStatus, Unit>

    @GET("/api/v1/users/onboarding-completed")
    suspend fun getOnboardingCompletedV2(): ApiResponse<OnboardingStatus?, Any?>

    @PATCH("/api/v1/users/name")
    suspend fun updateUserName(
        @Body request: UpdateNameRequest
    ): ApiResponse<UpdateNameRequest, List<FieldErrorDetail>>

    @PATCH("/api/v1/users/name")
    suspend fun updateUserNameV2(
        @Body request: UpdateNameRequest
    ): ApiResponse<Any, Any?>

    @PATCH("/api/v1/users/commute-info")
    suspend fun updateCommuteTime(
        @Body request: CommuteTimeRequest
    ): ApiResponse<EmptyResponse, Unit>

    @PATCH("/api/v1/users/commute-info")
    suspend fun updateCommuteInfo(
        @Body request: CommuteTimeRequest
    ): ApiResponse<Unit, Any?>
}