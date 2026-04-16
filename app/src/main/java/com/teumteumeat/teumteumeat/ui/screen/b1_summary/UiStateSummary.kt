package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType


data class UiStateSummary(
    /* ================= 진입 파라미터 ================= */
    val goalId: Long = -1,
    val goalType: DomainGoalType? = null,
    val documentId: Long = -1,
    val categoryId: Long? = null, // 요약글 화면에서는 사용x

    /* ================= 화면 상태 ================= */
    val isLoading: Boolean = false,
    val errorMessage: String? = null,


    val title: String = "",
    val dateText: String = "",
    val summary: String = "",
    val hasSolvedToday: Boolean = true,
    val isFirstTime: Boolean = true,

    /* category 전용 */
    val categoryDocumentId: Int = -1,

    /* 퀴즈 안내씬 서버에서 받은 분기 값 */
    val isQuizGuideSeen: Boolean = false,

    /* 퀴즈 안내씬 클라이언트에서 보낼 분기 값 */
    val isSkipQuizGuideChecked: Boolean = false,

)

