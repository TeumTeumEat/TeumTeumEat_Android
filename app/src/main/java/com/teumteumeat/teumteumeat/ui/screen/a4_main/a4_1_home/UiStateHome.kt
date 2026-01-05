package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.UiStateOnBoardingMainState

sealed class UiStateHomeState {

    /** 아무 일도 하지 않는 초기 상태 */
    data object Idle : UiStateHomeState()

    /** 로딩 화면 표시 */
    data object Loading : UiStateHomeState()

    /** 요청 성공 */
    data object Success : UiStateHomeState()

    /** 오류 발생 */
    data class Error(
        val message: String,
    ) : UiStateHomeState()
}

data class UiStateHome(
    val currentPage: Int = 0,
    val totalPage: Int = 5,

    val fireState: FireState = FireState.UnBurning,
    val stampCount: Int = 0,


    /** 🔥 핵심 */
    val snackState: SnackState = SnackState.Available
)

sealed class SnackState {

    /** ✅ 지금 간식 사용 가능 → 퀴즈 가능 */
    data object Available : SnackState()

    /** ⏳ 아직 도착 전 (오늘 시간 안 됨) */
    data class Waiting(
        val arrivalTime: String // "09:00"
    ) : SnackState()

    /** ❌ 오늘 이미 사용함 */
    data class Consumed(
        val nextArrivalTime: String // 내일 "09:00"
    ) : SnackState()

    /** 🚫 기간 종료 */
    data object Expired : SnackState()
}


enum class FeedingState{ }
enum class FireState { UnBurning, Burning }