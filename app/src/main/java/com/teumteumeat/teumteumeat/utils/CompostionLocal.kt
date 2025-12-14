package com.teumteumeat.teumteumeat.utils

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.UiStateOnBoardingMain
import com.teumteumeat.teumteumeat.ui.screen.a4_main.UiStateMain
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.UiStateHome
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.UiStateLibrary
import com.teumteumeat.teumteumeat.ui.theme.ExtendedColors


val LocalAppContext = compositionLocalOf<Context> {
    error("No Context provided") // Context가 제공되지 않으면 에러 발생
}

val LocalActivityContext = compositionLocalOf<ComponentActivity> {
    error("No ComponentActivity provided")
}

val LocalViewModelContext = compositionLocalOf<ViewModel>{
    error("No ViewModel provided")
}

val LocalExtendedColors = compositionLocalOf<ExtendedColors> {
    error("No ExtendedColors provided")
}

val LocalOnBoardingMainUiState = compositionLocalOf<UiStateOnBoardingMain> {
    error("No UiStateWelcome provided")
}

val LocalMainUiState = compositionLocalOf<UiStateMain> {
    error("No UiStateMain provided")
}

val LocalHomeUiState = compositionLocalOf<UiStateHome> {
    error("No UiStateHome provided")
}

val LocalLibraryUiState = compositionLocalOf<UiStateLibrary> {
    error("No UiStateLibrary provided")
}