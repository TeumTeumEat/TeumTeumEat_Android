package com.teumteumeat.teumteumeat.ui.screen.a0_splash


sealed interface SplashUiState {

    val nextRoute: SplashRoute?

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
    ) : SplashUiState
}

enum class SplashRoute {
    LOGIN,
    MAIN,
    ON_BOARDING,
}
