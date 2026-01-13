package com.teumteumeat.teumteumeat.ui.screen.a4_main

import androidx.compose.ui.geometry.Offset

data class UiStateMain(
    val currentPage: Int = 0,
    val totalPage: Int = 5,

    val currentStreak: Int = 0,
    val stampCount: Int = 0,
    val monthStampCount: Int = 0,

    val currentScreenType: MainScreenType = MainScreenType.MAIN,
    val hasHandledExternalNavigation: Boolean = false, // ✅ 추가

    val isExpandedBottomNavItemPlus: Boolean = false,
    val plusBtnOffset: Offset? = null,
)

enum class MainScreenType {
    MAIN,
    LIBRARY,
}

