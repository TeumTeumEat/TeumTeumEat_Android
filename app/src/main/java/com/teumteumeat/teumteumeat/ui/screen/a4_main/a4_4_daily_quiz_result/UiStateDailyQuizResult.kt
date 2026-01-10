package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_4_daily_quiz_result

import com.teumteumeat.teumteumeat.domain.model.common.GoalType
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.UiStateSummary
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultItem
import java.time.LocalDate
import java.time.LocalDateTime

data class UiStateDailyQuizResult(
    val isLoading: Boolean = false,

    /** 퀴즈 결과 API */
    val quizzes: List<QuizResultItem> = emptyList(),
    val createdAt: String = "",

    /** 파생 상태 */
    val correctCount: Int = 0,

    // 📌 요청 파라미터 (상태로 보존)
    val id: Long? = null,
    val type: GoalType? = null,
    val date: LocalDate? = null,

    /** 공통 에러 */
    val errorMessage: String? = null
)

