package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.UiStateSummary
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.TimeUtil.toMonthDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val quizRepository: QuizRepository,
    private val categoryRepository: CategoryRepository,
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateQuizResult())
    val uiState = _uiState.asStateFlow()

    fun loadUserGoal(
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            when (val result = goalRepository.getUserGoal()) {

                is ApiResultV2.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userGoal = result.data
                        )
                    }
                    onSuccess()
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.uiMessage
                        )
                    }
                }
            }
        }
    }

    fun initQuizResult(
        date: String
    ) {
        viewModelScope.launch {

            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            // 1️⃣ 현재 유저 목표 조회
            when (val goalResult = goalRepository.getUserGoal()) {

                is ApiResultV2.Success -> {
                    val userGoal = goalResult.data

                    _uiState.update {
                        it.copy(userGoal = userGoal)
                    }

                    // 2️⃣ 퀴즈 결과 조회
                    val goalType = userGoal.type
                    when(goalType){
                        DomainGoalType.CATEGORY -> {
                            val categoryId = userGoal.category?.categoryId
                            loadQuizResults(goalType.name, categoryId!!.toInt(), date)
                        }

                        DomainGoalType.DOCUMENT -> {
                            val documentId = userGoal.documentId
                            loadQuizResults(goalType.name, documentId!!.toInt(), date)
                        }
                    }

                    // 3️⃣ 목표 타입 기반 요약 조회
                    loadSummaryByGoal(userGoal)
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = goalResult.uiMessage
                        )
                    }
                }
            }
        }
    }

    private fun loadSummaryByGoal(
        goal: UserGoal
    ) {
        when (goal.type) {
            DomainGoalType.DOCUMENT -> {
                val goalId = goal.goalId.toInt()
                val documentId = goal.documentId ?: return
                loadDocumentSummary(goalId, documentId.toInt())
            }

            DomainGoalType.CATEGORY -> {
                val categoryId = goal.category?.categoryId ?: return
                loadCategorySummary(categoryId.toInt())
            }
        }
    }



    fun loadQuizResults(
        type: String,
        id: Int,
        date: String
    ) {
        viewModelScope.launch {

            // 2️⃣ API 호출
            when (val result = quizRepository.getQuizHistory(type, id, date)) {

                is ApiResultV2.Success -> {
                    val history = result.data

                    val quizzes = history.quizzes
                    val correctCount = quizzes.count { it.isCorrect }

                    // 3️⃣ 성공 상태 반영
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            quizzes = quizzes,
                            createdAt = history.createdAt,
                            correctCount = correctCount
                        )
                    }
                }

                is ApiResultV2.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResultV2.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResultV2.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * 오늘의 남남지식 요약 조회
     */
    fun loadDocumentSummary(goalId: Int, documentId: Int) {
        Log.d("TAG", "loadDocumentSummary: $goalId, $documentId")
        viewModelScope.launch {
            _uiState.update{
                it.copy(
                    isLoading = true,
                )
            }

            if (goalId == -1 || documentId == -1){
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "goalId 나 documentId 가 등록되지 않았습니다."
                    )
                }
            }

            when (val result = documentRepository.getDocumentSummary(goalId, documentId)) {

                is ApiResultV2.Success -> {
                    val data = result.data

                    _uiState.update {
                        it.copy(
                            summary = UiStateSummary(
                                title = data.fileName,
                                dateText = Utils.TimeUtil.todayText(),
                                summary = data.summary, // ⭐ 아래 유틸 참고
                                hasSolvedToday = data.hasSolvedToday,
                                isFirstTime = data.isFirstTime,
                                isLoading = false,
                                errorMessage = null,
                            ),
                            errorMessage = null,
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.uiMessage
                        )
                    }
                }
                /*is ApiResultV2.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResultV2.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "네트워크 연결을 확인해주세요."
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "로그인이 만료되었습니다. 다시 로그인해주세요."
                        )
                    }
                    // 👉 여기서 로그아웃 이벤트 트리거 가능
                }

                is ApiResultV2.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "알 수 없는 오류가 발생했습니다."
                        )
                    }
                }*/
            }
        }
    }

    fun loadCategorySummary(categoryId: Int) {
        viewModelScope.launch {
            _uiState.update{
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            if (categoryId == -1 ){
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "goalId 나 documentId 가 등록되지 않았습니다."
                    )
                }
            }

            // 2️⃣ 레포 호출
            when (
                val result =
                    categoryRepository.getDailyCategoryDocument(categoryId.toLong())
            ) {

                is ApiResultV2.Success -> {
                    val data = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = UiStateSummary(
                                dateText = toMonthDay(data.createdAt),
                                summary = data.content,
                                isFirstTime = data.isFirstTime,
                                categoryDocumentId = data.documentId.toInt(),
                                isLoading = false,
                                errorMessage = null,
                            ),
                            errorMessage = null,
                        )
                    }

                }

                is ApiResultV2.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.uiMessage
                        )
                    }
                }

                is ApiResultV2.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.uiMessage
                        )
                    }
                }

                is ApiResultV2.UnknownError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

}

