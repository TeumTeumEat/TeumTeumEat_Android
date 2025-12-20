package com.teumteumeat.teumteumeat.ui.screen.a0_splash


sealed interface SplashUiState{
    data object Idle : SplashUiState
    data object Loading : SplashUiState
    data object Success : SplashUiState
    data class Error(val message: String) : SplashUiState
}

