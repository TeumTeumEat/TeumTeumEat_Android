package com.teumteumeat.teumteumeat.data.repository.notification

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2

interface NotificationRepository {

    suspend fun registerDeviceToken(
        token: String,
        deviceType: String
    ): ApiResultV2<Unit>
}
