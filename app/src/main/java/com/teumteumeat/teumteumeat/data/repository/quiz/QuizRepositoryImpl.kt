package com.teumteumeat.teumteumeat.data.repository.quiz

import com.teumteumeat.teumteumeat.data.api.auth.AuthApiService
import com.teumteumeat.teumteumeat.data.api.quiz.QuizApiService
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model_request.SubmitUserQuizRequest
import com.teumteumeat.teumteumeat.data.network.model_response.QuizHistoryData
import com.teumteumeat.teumteumeat.data.network.model_response.UserQuiz
import com.teumteumeat.teumteumeat.data.network.model_response.toDomain
import com.teumteumeat.teumteumeat.data.repository.BaseRepository
import com.teumteumeat.teumteumeat.domain.mapper.toDomain
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.quiz.UserQuizStatus
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizHistory
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.SubmitQuizResult
import com.teumteumeat.teumteumeat.utils.Utils.RepositoryUtils.requireNotNullOrError
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val quizApiService: QuizApiService,
    private val authApiService: AuthApiService,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : BaseRepository(authApiService, tokenLocalDataSource), QuizRepository {

    override suspend fun getUserQuizStatus(): ApiResultV2<UserQuizStatus> {
        return safeApiVer2(
            apiCall = {
                quizApiService.getUserQuizStatus()
            },
            mapper = { response ->
                response
                    .requireNotNullOrError("/api/v1/user-quizzes/status")
                    .toDomain()
            }
        )
    }

    override suspend fun getQuizHistory(
        type: String,
        id: Int,
        date: String
    ): ApiResultV2<QuizHistory> {

        return safeApiVer2(
            apiCall = {
                quizApiService.getQuizHistory(
                    type = type,
                    id = id.toLong(),   // ⭐ Int → Long 변환
                    date = date
                )
            },
            mapper = { data: QuizHistoryData? ->
                data?.toDomain()
                    ?: error("QuizHistoryData is null")
            }
        )
    }

    override suspend fun submitQuiz(
        quizId: Int,
        userAnswer: String
    ): ApiResultV2<SubmitQuizResult> {

        return safeApiVer2(
            apiCall = {
                quizApiService.submitUserQuiz(
                    SubmitUserQuizRequest(
                        quizId = quizId,
                        userAnswer = userAnswer
                    )
                )
            },
            mapper = { data ->
                data?.toDomain()
                    ?: error("SubmitUserQuizResponse is null")
            }
        )
    }

    override suspend fun getUserQuizzes(
        documentId: Int,
        documentType: GoalTypeUiState
    ): ApiResultV2<List<UserQuiz>> {
        return safeApiVer2(
            apiCall = {
                quizApiService.getuserQuizzes(
                    documentId = documentId,
                    documentType = documentType.name
                )
            },
            mapper = { data ->
                data?.map { it.toDomain() } ?: emptyList()
            }
        )
    }

}
