package com.teumteumeat.teumteumeat.ui.screen.a4_main

data class UiStateMain(
    val currentPage: Int = 0,
    val totalPage: Int = 5,

    val currentStreak: Int = 0,
    val stampCount: Int = 0,
    val monthStampCount: Int = 0,

    val currentScreenType: MainScreenType = MainScreenType.MAIN
)

enum class MainScreenType {
    MAIN,
    LIBRARY,
}

