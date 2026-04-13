package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.UiStateSummary

data class UiStateQuizResult(
    val isLoading: Boolean = false,

    /** 퀴즈 결과 API */
/*    val quizHistory: QuizHistory = QuizHistory(
        createdAt = "",
        quizzes = emptyList(),
    ),*/
    val quizzes: List<QuizResultItem> = emptyList(),
    val createdAt: String = "",

    /** 파생 상태 */
    val correctCount: Int = -1,

    /** 카테고리 목표 요약글 문서Id */
    val categoryDocumentId: Long = -1,

    /** PDF / Summary API */
    val summary: UiStateSummary = UiStateSummary(isLoading = true),

    /** 공통 에러 */
    val errorMessage: String? = null,

    /** ✅ 현재 유저 목표 */
    val userGoal: UserGoal? = null,
)

data class QuizHistory(
    val createdAt: String,
    val quizzes: List<QuizResultItem>
)

data class QuizResultItem(
    val quizId: Int,
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String,
    val isCorrect: Boolean,
    val type: String
)
