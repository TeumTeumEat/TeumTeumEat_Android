package com.teumteumeat.teumteumeat.data.network.model_response

import com.teumteumeat.teumteumeat.ui.screen.b2_1_quiz_result.QuizHistory
import com.teumteumeat.teumteumeat.ui.screen.b2_1_quiz_result.QuizResultItem


data class QuizHistoryData(
    val createdAt: String,
    val quizzes: List<QuizHistoryQuizDto>
)

data class QuizHistoryQuizDto(
    val quizId: Long,
    val question: String,
    val options: List<String>,
    val answer: String,
    val type: String,
    val explanation: String,
    val isCorrect: Boolean
)

fun QuizHistoryData.toDomain(): QuizHistory {
    return QuizHistory(
        createdAt = createdAt,
        quizzes = quizzes.map { it.toDomain() }
    )
}

fun QuizHistoryQuizDto.toDomain(): QuizResultItem {
    return QuizResultItem(
        quizId = quizId.toInt(), // ⭐ Boundary 변환
        question = question,
        options = options,
        answer = answer,
        explanation = explanation,
        isCorrect = isCorrect,
        type = type
    )
}
