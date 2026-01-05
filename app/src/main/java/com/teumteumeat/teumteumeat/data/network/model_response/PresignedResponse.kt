package com.teumteumeat.teumteumeat.data.network.model_response

// 파일명: PresignedResponse.kt

data class PresignedResponse(
    val presignedUrl: String,
    val fileUrl: String,
    val key: String
)
