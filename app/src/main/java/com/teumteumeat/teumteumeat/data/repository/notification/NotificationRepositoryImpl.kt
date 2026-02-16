package com.teumteumeat.teumteumeat.data.repository.notification

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.notification.NotificationApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_request.notification.DeleteDeviceTokenRequest
import com.teumteumeat.teumteumeat.data.network.model_request.notification.DeviceType
import com.teumteumeat.teumteumeat.data.network.model_request.notification.RegisterDeviceTokenRequest
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApiService: NotificationApiService,
    authApiService: AuthApiService,
    tokenLocalDataSource: TokenLocalDataSource
) : BaseRepository(authApiService, tokenLocalDataSource),
    NotificationRepository {

    override suspend fun registerDeviceToken(
        token: String,
        deviceType: String
    ): ApiResultV2<Unit> {

        val request = RegisterDeviceTokenRequest(
            token = token,
            deviceType = DeviceType.valueOf(deviceType)
        )

        return safeApiVer2(
            apiCall = {
                notificationApiService.registerDeviceToken(request)
            },
            mapper = {
                // ✅ data 는 의미 없음 → Unit 반환
                Unit
            }
        )
    }

    override suspend fun deleteDeviceToken(
        token: String,
        deviceType: String
    ): ApiResultV2<Unit> {

        val request = DeleteDeviceTokenRequest(
            token = token,
            deviceType = DeviceType.valueOf(deviceType)
        )

        return safeApiVer2(
            apiCall = {
                notificationApiService.deleteDeviceToken(request)
            },
            mapper = {
                // ✅ data 는 의미 없음 → Unit 반환
                Unit
            }
        )
    }
}
