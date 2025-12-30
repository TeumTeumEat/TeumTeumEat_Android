package com.teumteumeat.teumteumeat.data.network.model_response

data class SocialLoginRequest(
    val idToken: String,
    val termsAgreed: Boolean,
    val name: String? = null,     // Apple 전용
    val authCode: String? = null  // Apple 전용
)

