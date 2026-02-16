package com.teumteumeat.teumteumeat.data.network.model_request.notification

data class DeleteDeviceTokenRequest(
    val token: String,
    val deviceType: DeviceType
)