package com.teumteumeat.teumteumeat.ui.screen.a1_login.state

import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider

data class PendingSocialLogin(
    val provider: SocialProvider,
    val idToken: String,
    val authCode: String? = null,
)