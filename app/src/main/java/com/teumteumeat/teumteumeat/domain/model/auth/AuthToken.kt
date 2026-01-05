package com.teumteumeat.teumteumeat.domain.model.auth

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val isOnboardingCompleted: Boolean
)
