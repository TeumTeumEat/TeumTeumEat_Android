package com.teumteumeat.teumteumeat.data.network.model_response

import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider

data class AccountInfoResponse(
    val socialProvider: SocialProvider,
    val email: String
)

fun AccountInfoResponse.toDomain(): AccountInfo =
    AccountInfo(
        socialProvider = socialProvider,
        email = email
    )

data class AccountInfo(
    val socialProvider: SocialProvider,
    val email: String
)