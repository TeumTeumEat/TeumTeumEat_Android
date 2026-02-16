package com.teumteumeat.teumteumeat.data.api.user

data class CommuteTimeRequest(
    val startTime: String, // "08:00:00"
    val endTime: String,   // "18:00:00"
    val usageTime: Int
)

