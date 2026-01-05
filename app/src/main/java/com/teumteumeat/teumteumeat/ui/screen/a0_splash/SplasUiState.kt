package com.teumteumeat.teumteumeat.ui.screen.a0_splash

import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState


data class SplashUiState (
    val isLoading: Boolean = false,
    // val errorMessage: String? = null,
    val errorState: ErrorState? = null,
    /*val nextRoute: SplashRoute?

    data object Idle : SplashUiState {
        override val nextRoute: SplashRoute? = null
    }

    data object Loading : SplashUiState {
        override val nextRoute: SplashRoute? = null
    }

    data class Success(
        override val nextRoute: SplashRoute
    ) : SplashUiState

    data class Error(
        val message: String,
        override val nextRoute: SplashRoute
    ) : SplashUiState*/
)

enum class SplashRoute {
    LOGIN,
    MAIN,
    ON_BOARDING,
}

sealed class SplashUiEvent {

    // 화면 이동
    data object NavigateToLogin : SplashUiEvent()
    data object NavigateToMain : SplashUiEvent()
    data object NavigateToOnboarding : SplashUiEvent()

    // 기타 (필요 시 확장)
    data class ShowErrorMessage(val message: String) : SplashUiEvent()
}
