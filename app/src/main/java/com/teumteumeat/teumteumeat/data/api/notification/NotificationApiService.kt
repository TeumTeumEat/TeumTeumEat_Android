package com.teumteumeat.teumteumeat.data.api.notification

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.EmptyResponse
import com.teumteumeat.teumteumeat.data.network.model_request.RegisterDeviceTokenRequest
import com.teumteumeat.teumteumeat.data.network.model_request.SubmitUserQuizRequest
import com.teumteumeat.teumteumeat.data.network.model_response.QuizGuideResponse
import com.teumteumeat.teumteumeat.data.network.model_response.QuizHistoryData
import com.teumteumeat.teumteumeat.data.network.model_response.SubmitUserQuizResponse
import com.teumteumeat.teumteumeat.data.network.model_response.UserQuizResponse
import com.teumteumeat.teumteumeat.data.network.model_response.quiz.UserQuizStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApiService {

    @POST("/api/v1/notifications/device-tokens")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): ApiResponse<EmptyResponse, Any?>
}
