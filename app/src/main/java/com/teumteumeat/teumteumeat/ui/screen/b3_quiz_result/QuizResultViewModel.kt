package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.datastore.GoalTrackingDataStore
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.domain.repository.pff_document.PdfDocumentRepository
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.domain.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.UiStateSummary
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pdfDocumentRepository: PdfDocumentRepository,
    private val quizRepository: QuizRepository,
    private val historyRepository: HistoryRepository,
    private val categoryRepository: CategoryRepository,
    private val goalRepository: GoalRepository,
    private val analyticsLogger: TeumAnalyticsLogger,
    private val goalTrackingDataStore: GoalTrackingDataStore,
    val sessionManager: SessionManager,
) : ViewModel() {

    companion object {
        private const val KEY_DOCUMENT_ID = "document_id"
        private const val KEY_DATE = "quiz_date"
        private const val KEY_TOPIC = "topic"
        private const val KEY_ENTRY_TYPE = "entry_type"
    }

    fun initArgs(
        documentId: Long,
        date: String,
        topic: String,
        entryType: String,
    ) {
        savedStateHandle[KEY_DOCUMENT_ID] = documentId
        savedStateHandle[KEY_DATE] = date
        savedStateHandle[KEY_TOPIC] = topic
        savedStateHandle[KEY_ENTRY_TYPE] = entryType
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

    fun getTopic(): String =
        savedStateHandle[KEY_TOPIC] ?: ""

    fun getEntryType(): String =
        savedStateHandle[KEY_ENTRY_TYPE] ?: "first"

    /**
     * QUIZ-005 — 퀴즈 결과 화면 "글보기" 버튼 탭 시 호출.
     * documentId/topic/entryType은 QuizActivity → QuizResultActivity Intent extra로 전달받은 값이다.
     */
    fun onReviewConceptTap() {
        analyticsLogger.logReviewConceptTap(
            contentId = getDocumentId().toString(),
            topic = getTopic(),
            entryType = getEntryType(),
        )
    }

    /** GOAL-001 course_complete 이벤트 중복 발송 방지 플래그. 발송 성공 시에만 true로 전환한다. */
    private var hasCourseCompleteLogged = false

    /**
     * GOAL-001 — 완주 화면(SubjectCompleteScreen) 진입 직전 호출.
     * userGoal은 [initQuizResult]에서 이미 로드된 값을 호출부(QuizResultNavHost)에서 그대로 전달받는다.
     * 완주 화면 재진입 등으로 중복 호출되어도 [hasCourseCompleteLogged]가 이미 true면 스킵한다.
     * total_stamps 조회(getCalendarHistory) 실패 시에는 stamp_earned와 동일하게 이벤트 발송 자체를
     * 보류하며, 이 경우 [hasCourseCompleteLogged]도 갱신하지 않아 다음 진입 시 재시도할 수 있다.
     */
    fun onCourseCompleteScreenEntered(userGoal: UserGoal) {
        if (hasCourseCompleteLogged) return

        viewModelScope.launch {
            val now = LocalDate.now()
            val totalStamps = when (
                val result = historyRepository.getCalendarHistory(now.year, now.monthValue)
            ) {
                is ApiResultV2.Success -> result.data.totalStamps.toLong()
                else -> return@launch
            }

            val categoryId = when (userGoal.type) {
                DomainGoalType.CATEGORY -> userGoal.category?.categoryId?.toString() ?: ""
                DomainGoalType.DOCUMENT -> userGoal.fileName ?: ""
            }
            val learningType = when (userGoal.type) {
                DomainGoalType.CATEGORY -> "category"
                DomainGoalType.DOCUMENT -> "pdf"
            }
            val goalWeeks = ChronoUnit.WEEKS.between(userGoal.startDate, userGoal.endDate)
            val isFirstComplete =
                goalTrackingDataStore.resolveAndMarkFirstComplete(userGoal.goalId.toString())

            hasCourseCompleteLogged = true
            analyticsLogger.logCourseComplete(
                goalId = userGoal.goalId.toString(),
                categoryId = categoryId,
                learningType = learningType,
                goalWeeks = goalWeeks,
                totalStamps = totalStamps,
                isFirstComplete = isFirstComplete.toString(),
            )
        }
    }

    fun initQuizResult() {
        viewModelScope.launch {
            val categoryDocumentId = getDocumentId()
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

                    // 2️⃣ 목표 타입 별 퀴즈 결과 조회
                    val goalType = userGoal.type
                    loadQuizResultByGoalType(goalType, userGoal, categoryDocumentId, date)

                    // 3️⃣ 목표 타입 기반 요약 조회
                    loadSummaryByGoal(userGoal, categoryDocumentId)
                    _screenState.value = UiScreenState.Success
                }

                else -> {
                    moveToError(goalResult)
                }
            }

        }
    }

    private suspend fun loadQuizResultByGoalType(
        type: DomainGoalType,
        goal: UserGoal,
        documentId: Long,
        date: String
    ) {
        when (type) {
            DomainGoalType.CATEGORY -> {
                // setCategoryDocumentId(goal.category!!.categoryId)
                loadQuizResults(type, documentId, date)
            }

            DomainGoalType.DOCUMENT -> {
                setDocumentSummaryId(goal.goalId, documentId)
                val pdfDocumentSummaryId = _uiState.value.pdfDocumentSummaryId
                loadQuizResults(type, pdfDocumentSummaryId.toLong(), date)
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
        goal: UserGoal,
        categoryDocumentId: Long
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
                val pdfDocumentSummaryId = uiState.value.pdfDocumentSummaryId
                loadDocumentSummary(goal.type, pdfDocumentSummaryId, date)
            }

            DomainGoalType.CATEGORY -> {
                loadCategoryGoalSummary(goal.type, categoryDocumentId, date)
            }
        }
    }

    private suspend fun setDocumentSummaryId(goalId: Long, documentId: Long) {
        when (val result =
            pdfDocumentRepository.getPdfDocumentSummaryId(goalId.toInt(), documentId.toInt())) {
            is ApiResultV2.Success -> {
                val data = result.data

                _uiState.update {
                    it.copy(
                        pdfDocumentSummaryId = data.value
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
    fun loadDocumentSummary(goalType: DomainGoalType, documentSummaryId: Int, date: String) {
        Log.d("loadDocumentSummary", "Pdf 문서 요약글 Id: $documentSummaryId")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                )
            }

            if (documentSummaryId == -1) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "documentId 를 전달받지 못했습니다. (id 전달 오류)"
                    )
                }
            }

            when (val result = historyRepository.getLearningHistorySummary(
                goalType, documentSummaryId.toLong(), date
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
        }
    }

    /**
     * 카테고리 목표 요약글 문서Id 조회 및 설정
     */
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
     * 카테고리 목표 요약글 조회
     */
    private fun loadCategoryGoalSummary(goalType: DomainGoalType, categoryId: Long, date: String) {

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

