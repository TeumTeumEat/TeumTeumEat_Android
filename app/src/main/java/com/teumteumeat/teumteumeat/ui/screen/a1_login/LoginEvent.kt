package com.teumteumeat.teumteumeat.ui.screen.a1_login

sealed interface LoginEvent {
    object KakaoLoginClicked : LoginEvent
}
