package com.teumteumeat.teumteumeat.data.repository.notification

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_request.notification.DeviceType

interface NotificationRepository {

    suspend fun registerDeviceToken(
        token: String,
        deviceType: String
    ): ApiResultV2<Unit>

    fun deleteDeviceToken(
        token: String,
        deviceType: String = DeviceType.ANDROID.name
    ): ApiResultV2<Unit>
}
