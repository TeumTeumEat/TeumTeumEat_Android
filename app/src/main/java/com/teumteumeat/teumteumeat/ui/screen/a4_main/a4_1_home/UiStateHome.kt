package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
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
    val summaryQuery: SummaryQuery = SummaryQuery(
        goalId = 0,
        goalType = DomainGoalType.DOCUMENT,
        documentId = 0,
        categoryId = 0
    ),

    // ================= 이미지 리소스 관련 =================
    val foodList: List<Int> = listOf(
        R.drawable.food_bungabbang, R.drawable.food_burger, R.drawable.food_cake,
        R.drawable.food_chicken, R.drawable.food_cookie, R.drawable.food_donut,
        R.drawable.food_fry, R.drawable.food_hotdog, R.drawable.food_icecream,
        R.drawable.food_kimbab, R.drawable.food_pizza, R.drawable.food_pudding,
        R.drawable.food_rice, R.drawable.food_salad, R.drawable.food_sandwich,
    ),

    // 현재 선택된 랜덤 음식 리소스 ID (기본값 설정 가능)
    val selectedFoodRes: Int = R.drawable.food_rice,

    val isShowGoalExpiredDialog: Boolean = false
){
    /**
     * 현재 상태에 따른 최종 이미지 리소스를 반환하는 헬퍼 함수
     */
    fun getDisplayFoodRes(): Int {
        return when (snackState) {
            is SnackState.Available -> selectedFoodRes
            else -> R.drawable.img_food_before // Available이 아닐 때 보여줄 기본 이미지
        }
    }
}

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

// 1. 사용할 이미지 리소스들을 리스트로 미리 정의합니다.
val foodList = listOf(
    R.drawable.food_bungabbang,
    R.drawable.food_burger,
    R.drawable.food_cake,
    R.drawable.food_chicken,
    R.drawable.food_cookie,
    R.drawable.food_donut,
    R.drawable.food_fry,
    R.drawable.food_hotdog,
    R.drawable.food_icecream,
    R.drawable.food_kimbab,
    R.drawable.food_pizza,
    R.drawable.food_pudding,
    R.drawable.food_rice,
    R.drawable.food_salad,
    R.drawable.food_sandwich,
)