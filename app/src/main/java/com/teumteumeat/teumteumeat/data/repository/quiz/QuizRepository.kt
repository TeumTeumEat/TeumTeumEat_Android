package com.teumteumeat.teumteumeat.data.repository.quiz

import com.teumteumeat.teumteumeat.data.network.model.ApiResponse
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.QuizHistoryData
import com.teumteumeat.teumteumeat.data.network.model_response.UserQuiz
import com.teumteumeat.teumteumeat.data.network.model_response.UserQuizResponse
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType
import com.teumteumeat.teumteumeat.ui.screen.b2_1_quiz_result.QuizHistory
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.SubmitQuizResult
import org.w3c.dom.DocumentType

interface QuizRepository {
    suspend fun getQuizHistory(
        type: String,
        id: Int,          // ✅ 앱 내부 표준
        date: String
    ): ApiResultV2<QuizHistory>

    suspend fun submitQuiz(
        quizId: Int,
        userAnswer: String
    ): ApiResultV2<SubmitQuizResult>

    suspend fun getUserQuizzes(
        documentId: Int,
        documentType: GoalType
    ): ApiResultV2<List<UserQuiz>>
}