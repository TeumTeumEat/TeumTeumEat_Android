package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType


data class UiStateHome(
    // ================= 페이징 =================
    val currentPage: Int = 0,
    val totalPage: Int = 5,

    // ================= 홈 상태 =================
    val fireState: FireState = FireState.UnBurning,
    val stampCount: Int = 0,
    val snackState: SnackState = SnackState.Available,

    /** 🔥 핵심 */
    val hasSolvedToday: Boolean = false,
    val isFirstTime: Boolean = false,

    // ================= 요약글 조회 핵심 =================
    val summaryQuery: SummaryQuery? = null
)

/**
 * 🔥 요약글 조회에 필요한 파라미터 묶음
 */
data class SummaryQuery(
    val goalId: Long,
    val goalType: DomainGoalType,      // ✅ 추가됨 (유저가 선택한 목표 타입)
    val documentId: Long?,       // optional
    val categoryId: Long?        // optional
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