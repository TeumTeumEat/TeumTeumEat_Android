package com.teumteumeat.teumteumeat.ui.screen.a1_login.webView

sealed interface KakaoLoginUiState {
    data object Idle : KakaoLoginUiState
    data object Loading : KakaoLoginUiState
    data object Success : KakaoLoginUiState
    data class Error(val message: String) : KakaoLoginUiState
}
