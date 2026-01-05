package com.teumteumeat.teumteumeat.data.network.model_response

import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.SubmitQuizResult

data class SubmitUserQuizResponse(
    val isCorrect: Boolean,
    val correctAnswer: String,
    val explanation: String
)

fun SubmitUserQuizResponse.toDomain(): SubmitQuizResult {
    return SubmitQuizResult(
        isCorrect = isCorrect,
        correctAnswer = correctAnswer,
        explanation = explanation
    )
}