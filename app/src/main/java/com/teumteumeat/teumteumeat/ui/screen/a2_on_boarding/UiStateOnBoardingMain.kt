package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

data class UiStateOnBoardingMain(
    val currentPage: Int = 0,
    val totalPage: Int = 5,

    // 이름 설정
    val charName: String = "",
    val errorMessage: String = "",
    val isValid: Boolean = false,
    val violation: NameViolation = NameViolation.None
)

sealed interface NameViolation {
    data object None : NameViolation
    data object Empty : NameViolation
    data object TooLong : NameViolation
    data object HasSpace : NameViolation
    data object HasSpecialChar : NameViolation
}