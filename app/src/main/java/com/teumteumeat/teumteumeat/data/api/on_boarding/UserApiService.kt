package com.teumteumeat.teumteumeat.data.api.on_boarding

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.domain.model.user.OnboardingStatus
import com.teumteumeat.teumteumeat.domain.model.user.UserName
import retrofit2.http.GET

interface UserApiService {
    @GET("/api/v1/users/onboarding-completed")
    suspend fun getOnboardingCompleted(): ApiResponse<OnboardingStatus>

/*    @POST("/api/v1/users/reissue")
    suspend fun reissueAccessToken(
        @Body refreshToken: ResponseBody
    ): ApiResponse<String>*/
}