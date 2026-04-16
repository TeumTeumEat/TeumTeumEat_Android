package com.teumteumeat.teumteumeat.data.repository.quiz

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model_response.UserQuiz
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.quiz.UserQuizStatus
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizHistory
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.SubmitQuizResult

interface QuizRepository {
    suspend fun confirmQuizGuide(): ApiResultV2<Boolean>

    suspend fun getUserQuizStatus(): ApiResultV2<UserQuizStatus>

    suspend fun getQuizHistory(
        type: DomainGoalType,
        id: Long,
        date: String
    ): ApiResultV2<QuizHistory>

    suspend fun submitQuiz(
        quizId: Int,
        userAnswer: String
    ): ApiResultV2<SubmitQuizResult>

    suspend fun getUserQuizzes(
        documentId: Int,
        documentType: GoalTypeUiState
    ): ApiResultV2<List<UserQuiz>>

    suspend fun getAdReward() : ApiResultV2<Unit>

    suspend fun submitCompleteQuizSet() : ApiResultV2<Unit>
}