package com.teumteumeat.teumteumeat.utils

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.UiStateOnBoardingMain
import com.teumteumeat.teumteumeat.ui.screen.a4_main.UiStateMain
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.UiStateHome
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.UiStateLibrary
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.UiStateSummary
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.UiStateQuizResult
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.UiStateQuiz
import com.teumteumeat.teumteumeat.ui.screen.c1_mypage.UiStateMyPage
import com.teumteumeat.teumteumeat.ui.screen.c2_goal_list.UiStateGoalList
import com.teumteumeat.teumteumeat.ui.screen.c3_edit_user_info.UiStateEditUserInfo
import com.teumteumeat.teumteumeat.ui.theme.AppTypography
import com.teumteumeat.teumteumeat.ui.theme.DefaultAppTypography
import com.teumteumeat.teumteumeat.ui.theme.ExtendedColors


val LocalAppContext = compositionLocalOf<Context> {
    error("No Context provided") // Context가 제공되지 않으면 에러 발생
}

val LocalActivityContext = compositionLocalOf<ComponentActivity> {
    error("No ComponentActivity provided")
}

val LocalAppTypography = staticCompositionLocalOf { DefaultAppTypography }

val MaterialTheme.appTypography: AppTypography
    @Composable
    get() = LocalAppTypography.current

val LocalExtendedColors = compositionLocalOf<ExtendedColors> { error("No ExtendedColors provided") }
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current



val LocalViewModelContext = compositionLocalOf<ViewModel>{
    error("No ViewModel provided")
}

val LocalEditUserInfoUiState = compositionLocalOf<UiStateEditUserInfo> {
    error("No UiStateEditUserInfo provided")
}

val LocalGoalListUiState = compositionLocalOf<UiStateGoalList> {
    error("No UiStateGoalList provided")
}

val LocalMyPageUiState = compositionLocalOf<UiStateMyPage> {
    error("No UiStateMyPage provided")
}

val LocalOnBoardingMainUiState = compositionLocalOf<UiStateOnBoardingMain> {
    error("No UiStateWelcome provided")
}


val LocalScreenState = compositionLocalOf<UiScreenState>{
    error("No UiScreenState provided")
}

val LocalSummaryUiState = compositionLocalOf<UiStateSummary> {
    error("No UiStateSummary provided")
}

val LocalQuizUiState = compositionLocalOf<UiStateQuiz> {
    error("No UiStateQuiz provided")
}

val LocalQuizResultUiState = compositionLocalOf<UiStateQuizResult> {
    error("No UiStateQuizResult provided")
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