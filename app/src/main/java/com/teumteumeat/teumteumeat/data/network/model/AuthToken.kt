package com.teumteumeat.teumteumeat.data.network.model

import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val socialLoginType: String? = null,
)
