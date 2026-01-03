package com.teumteumeat.teumteumeat.data.network.model_request

data class SubmitUserQuizRequest(
    val quizId: Int,
    val userAnswer: String
)
