package com.teumteumeat.teumteumeat.data.network.model_response

import com.google.gson.annotations.SerializedName
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizType

data class UserQuizResponse(
    val quizId: Int,
    val question: String,
    val options: List<String>,
    val type: String
)


data class UserQuiz(
    val quizId: Int,
    val question: String,
    val options: List<String>,
    val type: QuizType
)

fun UserQuizResponse.toDomain(): UserQuiz {
    return UserQuiz(
        quizId = quizId,
        question = question,
        options = options,
        type = QuizType.from(type)
    )
}

