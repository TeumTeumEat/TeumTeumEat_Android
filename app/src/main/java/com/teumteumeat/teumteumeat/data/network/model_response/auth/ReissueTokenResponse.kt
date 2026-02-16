package com.teumteumeat.teumteumeat.data.network.model_response.auth

data class ReissueTokenResponse(
    val accessToken: String,
    val refreshToken: String? // 🔹 선택적
)