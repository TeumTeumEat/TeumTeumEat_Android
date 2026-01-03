package com.teumteumeat.teumteumeat.data.network.model_response

import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizType

data class QuizListResponse(
    val quizzes: List<QuizResponse>
)

data class QuizResponse(
    val quizId: Long,
    val question: String,
    val options: List<String>,
    val answer: String,
    val type: String,
    val explanation: String
)

