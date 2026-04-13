package com.teumteumeat.teumteumeat.data.api.notification

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.EmptyResponse
import com.teumteumeat.teumteumeat.data.network.model_request.notification.DeleteDeviceTokenRequest
import com.teumteumeat.teumteumeat.data.network.model_request.notification.RegisterDeviceTokenRequest
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface NotificationApiService {

    @POST("/api/v1/notifications/device-tokens")
    suspend fun registerDeviceToken(
        @Body request: RegisterDeviceTokenRequest
    ): ApiResponse<EmptyResponse, Any?>

    @HTTP(method = "DELETE", path = "/api/v1/notifications/device-tokens", hasBody = true)
    suspend fun deleteDeviceToken(
        @Body request: DeleteDeviceTokenRequest
    ): ApiResponse<EmptyResponse, Any?>
}
