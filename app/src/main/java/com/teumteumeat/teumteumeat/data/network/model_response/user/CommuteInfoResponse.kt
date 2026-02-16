package com.teumteumeat.teumteumeat.data.network.model_response.user

data class CommuteInfoResponse(
    val startTime: String, // "HH:mm:ss"
    val endTime: String,   // "HH:mm:ss"
    val usageTime: Int     // 분 단위
)