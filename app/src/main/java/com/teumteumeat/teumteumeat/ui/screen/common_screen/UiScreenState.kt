package com.teumteumeat.teumteumeat.ui.screen.common_screen

sealed class UiScreenState {

    /** 아무 일도 하지 않는 초기 상태 */
    data object Idle : UiScreenState()

    /** 로딩 화면 표시 */
    data object Loading : UiScreenState()

    /** 등록 성공 */
    data object Success : UiScreenState()

    /** 오류 발생 */
    data class Error(
        val message: String
    ) : UiScreenState()
}