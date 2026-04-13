package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.domain.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.UiStateSummary
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val documentRepository: DocumentRepository,
    private val quizRepository: QuizRepository,
    private val historyRepository: HistoryRepository,
    private val categoryRepository: CategoryRepository,
    private val goalRepository: GoalRepository,
    val sessionManager: SessionManager,
) : ViewModel() {

    companion object {
        private const val KEY_DOCUMENT_ID = "document_id"
        private const val KEY_DATE = "quiz_date"
    }

    fun initArgs(
        documentId: Long,
        date: String
    ) {
        savedStateHandle[KEY_DOCUMENT_ID] = documentId
        savedStateHandle[KEY_DATE] = date
    }

    private val _uiState = MutableStateFlow(UiStateQuizResult())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Loading)
    val screenState = _screenState.asStateFlow()


    fun getDocumentId(): Long =
        savedStateHandle[KEY_DOCUMENT_ID] ?: error("documentId missing")

    fun getDate(): String =
        savedStateHandle[KEY_DATE] ?: error("date missing")

    fun initQuizResult() {
        viewModelScope.launch {
            val documentId = getDocumentId()
            val date = getDate()
            _screenState.value = UiScreenState.Loading
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
                    when (goalType) {
                        DomainGoalType.CATEGORY -> {
                            loadQuizResults(goalType, documentId, date)
                        }

                        DomainGoalType.DOCUMENT -> {
                            val documentId = userGoal.documentId
                            loadQuizResults(goalType, documentId!!, date)
                        }
                    }

                    // 3️⃣ 목표 타입 기반 요약 조회
                    loadSummaryByGoal(userGoal)
                    _screenState.value = UiScreenState.Success
                }

                else -> {
                    moveToError(goalResult)
                }
            }

        }
    }

    fun loadQuizResults(
        type: DomainGoalType,
        id: Long,
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

                else -> {
                    moveToError(result)
                }
            }
        }
    }


    private suspend fun loadSummaryByGoal(
        goal: UserGoal
    ) {
        val date = getDate() // ViewModel에 저장된 퀴즈 날짜 사용

        val goalId = goal.goalId.toInt()

        if (goalId == -1) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "goalId 를 전달받지 못했습니다. (id 전달 오류)"
                )
            }
        }

        when (goal.type) {
            DomainGoalType.DOCUMENT -> {
                val documentId = goal.documentId ?: return
                loadDocumentSummary(goal.type, documentId, date)
            }

            DomainGoalType.CATEGORY -> {
                val categoryId = goal.category?.categoryId ?: return
                setCategoryDocumentId(categoryId)
                val categoryDocumentId = uiState.value.categoryDocumentId
                loadCategorySummary(goal.type, categoryDocumentId, date)
            }
        }
    }

    private suspend fun setCategoryDocumentId(categoryId: Long) {

        when (val result = categoryRepository.getDailyCategoryDocument(categoryId)) {
            is ApiResultV2.Success -> {
                val data = result.data

                _uiState.update {
                    it.copy(
                        categoryDocumentId = data.documentId
                    )
                }

            }

            else -> {
                moveToError(result)
            }
        }
    }

    /**
     * 오늘의 남남지식 요약 조회
     */
    fun loadDocumentSummary(goalType: DomainGoalType, documentId: Long, date: String) {
        Log.d("loadDocumentSummary", "요약글 번호: $documentId")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                )
            }

            if (documentId == -1L) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "존재하지 않는 documentId 입니다.(요약글 조회 오류)"
                    )
                }
            }

            when (val result = historyRepository.getLearningHistorySummary(
                goalType, documentId, date
            )
            ) {
                is ApiResultV2.Success -> {
                    val data = result.data

                    _uiState.update {
                        it.copy(
                            summary = UiStateSummary(
                                title = data.title,
                                dateText = Utils.TimeUtil.todayText(),
                                summary = data.summary, // ⭐ 아래 유틸 참고
                                isLoading = false,
                                errorMessage = null,
                            ),
                            errorMessage = null,
                        )
                    }

                }

                else -> {
                    moveToError(result)
                }
            }

            /*when (val result = documentRepository.getDocumentSummary(goalId, documentId)) {

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
                    moveToError(result)
                }
            }*/
        }
    }


    fun loadCategorySummary(goalType: DomainGoalType, categoryId: Long, date: String) {

        viewModelScope.launch {
            if (categoryId == -1L) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "documentId 가 없습니다. (요약글 조회 오류)"
                    )
                }
            }

            // 2️⃣ 카테고리 목표 요약글 API 호출
            when (val result = historyRepository.getLearningHistorySummary(
                goalType, categoryId, date
            )
            ) {
                is ApiResultV2.Success -> {
                    val data = result.data

                    _uiState.update {
                        it.copy(
                            summary = UiStateSummary(
                                title = data.title,
                                dateText = Utils.TimeUtil.todayText(),
                                summary = data.summary, // ⭐ 아래 유틸 참고
                                isLoading = false,
                                errorMessage = null,
                            ),
                            errorMessage = null,
                        )
                    }

                }

                else -> {
                    moveToError(result)
                }
            }

            /*when (
                val result =
                    categoryRepository.getDailyCategoryDocument(categoryId.toLong())
            ) {

                is ApiResultV2.Success -> {
                    val data = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = UiStateSummary(
                                title = data.title,
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

                else -> {
                    moveToError(result)
                }

            }*/
        }
    }

    /**
     * 에러 처리 함수
     */
    private suspend fun moveToError(result: ApiResultV2<*>) {

        when (result) {
            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
            }

            else -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.uiMessage
                    )
                }
                _screenState.value =
                    UiScreenState.Error(result.uiMessage)
            }
        }

    }


}

