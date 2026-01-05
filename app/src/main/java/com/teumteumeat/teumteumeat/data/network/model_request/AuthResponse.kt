package com.teumteumeat.teumteumeat.data.network.model_request

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val isOnboardingCompleted: Boolean
)