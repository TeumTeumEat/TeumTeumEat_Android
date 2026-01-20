package com.teumteumeat.teumteumeat.data.network.model_request

data class RegisterDeviceTokenRequest(
    val token: String,
    val deviceType: DeviceType
)

enum class DeviceType {
    IOS,
    ANDROID
}