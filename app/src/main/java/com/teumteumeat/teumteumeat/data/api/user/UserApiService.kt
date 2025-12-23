package com.teumteumeat.teumteumeat.data.api.user

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.EmptyResponse
import com.teumteumeat.teumteumeat.data.network.model.FieldErrorDetail
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingStatus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApiService {
    @GET("/api/v1/users/onboarding-completed")
    suspend fun getOnboardingCompleted(): ApiResponse<OnboardingStatus, Unit>

    @POST("/api/v1/users/name")
    suspend fun updateUserName(
        @Body request: UpdateNameRequest
    ): ApiResponse<UpdateNameRequest, List<FieldErrorDetail>>

    @PATCH("/api/v1/users/commute-info")
    suspend fun updateCommuteTime(
        @Body request: CommuteTimeRequest
    ): ApiResponse<EmptyResponse, Unit>
}