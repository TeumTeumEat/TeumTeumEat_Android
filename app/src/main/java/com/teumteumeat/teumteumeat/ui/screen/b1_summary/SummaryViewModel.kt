package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import android.app.Application
import android.util.Log
import com.teumteumeat.teumteumeat.BuildConfig
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.domain.repository.pff_document.PdfDocumentRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.sse.SseBusinessException
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.document.StreamPdfSummaryUseCase
import com.teumteumeat.teumteumeat.domain.usecase.summary.StreamDailySummaryUseCase
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.TimeUtil.toMonthDay
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UiEvent {
    data object MoveToQuiz : UiEvent
    /** 홈으로 이동 — 목표 완료/기간 종료(GOAL-002/003) 시 새 목표 안내 다이얼로그를 띄우기 위함. */
    data object MoveToHome : UiEvent
    data class ShowError(val message: String) : UiEvent
}

/** 목표 학습 횟수 완료 — 홈 이동 후 새 목표 안내. */
private const val ERROR_CODE_GOAL_COMPLETED = "GOAL-003"

/** 목표 학습 기간 종료 — 홈 이동 후 새 목표 안내. */
private const val ERROR_CODE_GOAL_EXPIRED = "GOAL-002"

/** 퀴즈 풀이 횟수 소진 — 홈 이동 후 안내. */
private const val ERROR_CODE_QUIZ_EXHAUSTED = "QUIZ-002"

/** 미완료 요약글/퀴즈 존재 — 기존 요약글 GET 조회. */
private const val ERROR_CODE_QUIZ_EXISTS = "QUIZ-003"


@dagger.hilt.android.lifecycle.HiltViewModel
class SummaryViewModel @Inject constructor(
    private val pdfDocumentRepository: PdfDocumentRepository,
    private val categoryRepository: CategoryRepository,
    private val quizRepository: QuizRepository,
    private val streamDailySummaryUseCase: StreamDailySummaryUseCase,
    private val streamPdfSummaryUseCase: StreamPdfSummaryUseCase,
    val application: Application,
    val sessionManager: SessionManager,
    private val analyticsLogger: TeumAnalyticsLogger,
) : ViewModel() {

    private val appContext = application.applicationContext

    /** ViewModel 인스턴스당 summary_view_start 이벤트 중복 발송 방지 플래그 */
    private var hasSummaryViewStartLogged = false

    /** ViewModel 인스턴스당 summary_view_complete 이벤트 중복 발송 방지 플래그 */
    private var hasSummaryViewCompleteLogged = false

    private val _uiState = MutableStateFlow(UiStateSummary())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private val _event = MutableSharedFlow<UiEvent>()
    val event: SharedFlow<UiEvent> = _event

    /** CATEGORY·DOCUMENT 모두 SSE 경로. loadInitialData에서 Success를 조기 설정하지 않기 위한 플래그. */
    private var shouldForceStream = false

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _screenState.value = UiScreenState.Loading

            val job1 = launch { loadUserQuizStatus() }
            val job2 = launch { loadSummaryByGoalType() }

            job1.join()
            job2.join()

            // CATEGORY·DOCUMENT 모두 SSE 경로이므로 Success 전환은 각 SSE 핸들러가 담당한다.
            if (!shouldForceStream && _screenState.value !is UiScreenState.Error) {
                _screenState.value = UiScreenState.Success
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateSkipGuideSceneFlag(isSkipQuizGuideChecked: Boolean) {
        _uiState.update {
            it.copy(isSkipQuizGuideChecked = !isSkipQuizGuideChecked)
        }
    }

    private fun loadUserQuizStatus() = viewModelScope.launch {
        when (val result = quizRepository.getUserQuizStatus()) {

            is ApiResultV2.Success -> {
                _uiState.update {
                    it.copy(
                        isQuizGuideSeen = result.data.isQuizGuideSeen,
                        hasSolvedTodayGlobal = result.data.hasSolvedToday,
                    )
                }
            }

            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
            }

            else -> {
                _event.emit(UiEvent.ShowError(result.uiMessage))
            }
        }
    }


    /**
     * 퀴즈 시작 버튼 클릭 처리
     */
    fun onQuizClick(isSkipQuizGuideChecked: Boolean) {
        viewModelScope.launch {

            // ✅ isFirstTime false 인 경우 → 서버에 확인 처리
            if (isSkipQuizGuideChecked) {
                when (val result = quizRepository.confirmQuizGuide()) {

                    is ApiResultV2.Success -> {
                        _event.emit(UiEvent.MoveToQuiz)
                    }

                    is ApiResultV2.SessionExpired -> {
                        sessionManager.expireSession()
                    }

                    else -> {
                        _event.emit(
                            UiEvent.ShowError(result.uiMessage)
                        )
                    }
                }
            } else {
                // ✅ true 인 경우 → 바로 이동
                _event.emit(UiEvent.MoveToQuiz)
            }
        }
    }

    fun loadSummaryByGoalType() {
        viewModelScope.launch {
            val currentState = _uiState.value
            try {
                when (currentState.goalType) {
                    DomainGoalType.CATEGORY -> {
                        val cId = currentState.categoryId ?: run {
                            handleInvalidParam("categoryId 가 등록되지 않았습니다.")
                            return@launch
                        }
                        // 항상 SSE로 생성 시도. 에러 발생 시 handleSummaryStreamError 에서 GET 폴백.
                        startCategorySummaryStream(cId)
                    }
                    DomainGoalType.DOCUMENT -> {
                        // OCR 처리는 AddGoalViewModel 에서 완료됨 → PDF 요약글 SSE 스트리밍 시작
                        startPdfSummaryStream(currentState.goalId, currentState.documentId)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.d("SummaryViewModel", "요약 로딩 실패: ${e.message}")
                _screenState.value = UiScreenState.Error(message = "요약 로딩 실패")
            }
        }
    }

    private fun handleInvalidParam(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message
            )
        }
        _screenState.value = UiScreenState.Error(message)
    }


    fun initSummary(
        goalId: Long,
        goalType: DomainGoalType,
        documentId: Long,
        categoryId: Long?,
        forceStream: Boolean = false
    ) {
        Log.d("SummaryViewModel", "goalId: $goalId, goalType: $goalType, documentId: $documentId, categoryId: $categoryId, forceStream: $forceStream")

        // CATEGORY·DOCUMENT 모두 SSE 경로이므로 Success 전환은 각 SSE 핸들러가 담당한다.
        shouldForceStream = goalType == DomainGoalType.CATEGORY || goalType == DomainGoalType.DOCUMENT
        _uiState.update {
            it.copy(
                goalId = goalId,
                goalType = goalType,
                documentId = documentId.toLong(),
                categoryId = categoryId,
                categoryDocumentId = categoryId?.toInt() ?: -1,
            )
        }

        loadInitialData()
    }

    /**
     * 카테고리 요약글을 SSE 스트리밍으로 생성하고, 완료 후 GET으로 전체 데이터를 로드한다.
     *
     * ### 이벤트 처리
     * - [SseEvent.Connected]     : 연결 수립 — 로딩 상태 유지
     * - [SseEvent.Chunk]         : 청크 수신 → 로그 + 실시간 화면 표시
     * - [SseEvent.TitleReceived] : 스트리밍 완료 → GET으로 최종 데이터 로드
     * - [SseEvent.StreamError]   : 오류 → 에러 화면 표시
     */
    private fun startCategorySummaryStream(categoryId: Long) {
        viewModelScope.launch {
            _screenState.value = UiScreenState.Loading
            _uiState.update { it.copy(isLoading = true, errorMessage = null, summary = "") }

            val buffer = StringBuilder()

            streamDailySummaryUseCase(categoryId).collect { event ->
                when (event) {
                    is SseEvent.Connected -> {
                        // SSE 연결 수립 — 로딩 상태 유지
                    }
                    is SseEvent.Chunk -> {
                        if (BuildConfig.DEBUG) Log.d("SummaryStream", "chunk: ${event.text}")
                        buffer.append(event.text)
                        // 전체 버퍼를 그대로 MarkdownText에 전달 → 스트리밍 중에도 마크다운 라이브 렌더링.
                        // 미완성 마커(**, `, ```)는 MarkdownText 내부에서 노출하지 않는다.
                        _uiState.update { it.copy(summary = buffer.toString(), isStreaming = true) }
                        _screenState.value = UiScreenState.Success
                    }
                    is SseEvent.TitleReceived -> {
                        // 스트리밍 완료 → 요약글 GET + 퀴즈 프리페치 순차 실행
                        _uiState.update { it.copy(isStreaming = false, isQuizLoading = true) }
                        if (!hasSummaryViewStartLogged) {
                            hasSummaryViewStartLogged = true
                            analyticsLogger.logSummaryViewStart(
                                sessionId = _uiState.value.goalId.toString(),
                                contentId = documentId.toString(),
                                topic = summary.fileName,
                            )
                        }
                        viewModelScope.launch {
                            // 요약 GET·퀴즈 프리페치가 중단되어도 버튼이 "퀴즈를 불러오는 중..."에
                            // 고정되지 않도록 finally 에서 isQuizLoading 을 반드시 내린다.
                            try {
                                loadCategorySummaryInternal(categoryId.toInt())
                                val documentId = _uiState.value.categoryDocumentId
                                if (documentId != -1) prefetchQuiz(documentId, GoalTypeUiState.CATEGORY)
                            } finally {
                                _uiState.update { it.copy(isQuizLoading = false) }
                            }
                        }
                    }
                    is SseEvent.StreamError -> {
                        handleSummaryStreamError(event.throwable, categoryId)
                    }
                }
            }
        }
    }

    /**
     * 요약 스트리밍 에러를 비즈니스 코드별로 분기 처리한다.
     *
     * - `GOAL-002`/`GOAL-003` : 홈으로 이동 → 새 목표 안내 다이얼로그
     * - `QUIZ-002`            : 기존 요약글 GET 조회 (재생성 불가)
     * - 그 외                  : 에러 화면 표시
     */
    private suspend fun handleSummaryStreamError(throwable: Throwable, categoryId: Long) {
        val errorCode = (throwable as? SseBusinessException)?.errorCode
        Log.e("SSE_ERROR", "SSE 요약 에러 수신: errorCode=$errorCode, message=${throwable.message}")
        _uiState.update { it.copy(isLoading = false, isStreaming = false) }

        when (errorCode) {
            ERROR_CODE_GOAL_COMPLETED,
            ERROR_CODE_GOAL_EXPIRED -> {
                Log.d("SSE_ERROR", "목표 완료/기간 종료 → 홈 이동")
                _event.emit(UiEvent.MoveToHome)
            }

            else -> {
                // QUIZ-002, 네트워크 오류, 그 외 모든 에러 → GET으로 기존 요약글 조회 (폴백)
                Log.d("SSE_ERROR", "SSE 오류 → GET 폴백: categoryId=$categoryId")
                loadCategorySummary(categoryId.toInt())
            }
        }
    }

    /**
     * PDF 요약글을 SSE 스트리밍으로 생성하고, 완료 후 GET으로 최종 메타데이터를 로드한다.
     *
     * ### 이벤트 처리
     * - [SseEvent.Connected]     : 처리 프로그레스 숨기기
     * - [SseEvent.Chunk]         : 청크 수신 → 실시간 화면 표시 (isStreaming = true)
     * - [SseEvent.TitleReceived] : 스트리밍 완료 → GET으로 최종 데이터 로드
     * - [SseEvent.StreamError]   : 에러 코드 분기 처리
     */
    private suspend fun startPdfSummaryStream(goalId: Long, documentId: Long) {
        _screenState.value = UiScreenState.Loading
        _uiState.update { it.copy(summary = "", isStreaming = false) }
        val buffer = StringBuilder()

        streamPdfSummaryUseCase(goalId, documentId).collect { event ->
            when (event) {
                is SseEvent.Connected -> {
                    // 연결 수립 — 로딩 상태 유지
                }
                is SseEvent.Chunk -> {
                    if (BuildConfig.DEBUG) Log.d("PdfSummaryStream", "chunk: ${event.text}")
                    buffer.append(event.text)
                    _uiState.update { it.copy(summary = buffer.toString(), isStreaming = true) }
                    _screenState.value = UiScreenState.Success
                }
                is SseEvent.TitleReceived -> {
                    // 스트리밍 완료 → 요약글 GET + PDF 퀴즈 프리페치 순차 실행
                    _uiState.update { it.copy(title = event.title, isStreaming = false, isQuizLoading = true) }
                    try {
                        fetchDocumentSummary(goalId.toInt(), documentId.toInt())
                        prefetchQuiz(documentId.toInt(), GoalTypeUiState.DOCUMENT)
                    } finally {
                        _uiState.update { it.copy(isQuizLoading = false) }
                    }
                }
                is SseEvent.StreamError -> {
                    handlePdfSummaryStreamError(event.throwable, goalId.toInt(), documentId.toInt())
                }
            }
        }
    }

    /**
     * PDF 요약 스트리밍 에러를 비즈니스 코드별로 분기 처리한다.
     *
     * - `GOAL-003` : 홈 이동 → 새 목표 안내 다이얼로그
     * - `QUIZ-002` : 홈 이동 → 퀴즈 횟수 소진 안내
     * - `QUIZ-003` : 기존 요약글 GET 조회 (미완료 요약/퀴즈 존재)
     * - 그 외       : 에러 화면 표시
     */
    private suspend fun handlePdfSummaryStreamError(throwable: Throwable, goalId: Int, documentId: Int) {
        val errorCode = (throwable as? SseBusinessException)?.errorCode
        Log.e("SSE_ERROR", "PDF 요약 SSE 에러 수신: errorCode=$errorCode, message=${throwable.message}")
        _uiState.update { it.copy(isStreaming = false) }

        when (errorCode) {
            ERROR_CODE_GOAL_COMPLETED -> _event.emit(UiEvent.MoveToHome)
            ERROR_CODE_QUIZ_EXHAUSTED -> _event.emit(UiEvent.MoveToHome)
            ERROR_CODE_QUIZ_EXISTS    -> {
                Log.d("SSE_ERROR", "QUIZ-003 → GET 폴백: goalId=$goalId, documentId=$documentId")
                fetchDocumentSummary(goalId, documentId)
            }
            else -> {
                _screenState.value = UiScreenState.Error(
                    throwable.message ?: "요약글 생성에 실패했습니다."
                )
            }
        }
    }

    private suspend fun fetchDocumentSummary(goalId: Int, documentId: Int) {
        when (val result = pdfDocumentRepository.getPdfDocumentSummary(goalId, documentId)) {
            is ApiResultV2.Success -> {
                val summary = result.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = summary.fileName,
                        dateText = toMonthDay(summary.updatedAt),
                        summary = summary.summary,
                        errorMessage = null
                    )
                }
                _screenState.value = UiScreenState.Success
            }

            is ApiResultV2.SessionExpired -> sessionManager.expireSession()

            else -> {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.uiMessage) }
                _screenState.value = UiScreenState.Error("문서 조회에 실패하였습니다.")
            }
        }
    }


    /**
     * 스크롤 최하단 도달 + 콘텐츠 완전 수신 시 Screen에서 호출.
     * ViewModel 인스턴스당 최초 1회만 로깅합니다.
     */
    fun logSummaryViewComplete() {
        if (hasSummaryViewCompleteLogged) return
        val state = _uiState.value
        val contentId = when (state.goalType) {
            DomainGoalType.CATEGORY -> state.categoryDocumentId.toString()
            DomainGoalType.DOCUMENT -> state.documentId.toString()
            else -> return
        }
        hasSummaryViewCompleteLogged = true
        analyticsLogger.logSummaryViewComplete(
            sessionId = state.goalId.toString(),
            contentId = contentId,
            topic = state.title,
        )
    }

    fun resetIdleState() {
        _screenState.value = UiScreenState.Idle
    }

    fun loadCategorySummary(categoryId: Int) {
        viewModelScope.launch { loadCategorySummaryInternal(categoryId) }
    }

    private suspend fun loadCategorySummaryInternal(categoryId: Int) {
        _uiState.update {
            it.copy(
                categoryId = categoryId.toLong(),
                isLoading = true,
                errorMessage = null,
            )
        }

        if (categoryId == -1) {
            _screenState.value = UiScreenState.Error("categoryId 가 등록되지 않았습니다.")
            _uiState.update { it.copy(isLoading = false, errorMessage = "categoryId 가 등록되지 않았습니다.") }
            return
        }

        when (val result = categoryRepository.getDailyCategoryDocument(categoryId.toLong())) {
            is ApiResultV2.Success -> {
                val data = result.data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = data.title,
                        summary = data.content,
                        hasSolvedToday = data.hasSolvedToday,
                        isFirstTime = data.isFirstTime,
                        dateText = toMonthDay(data.createdAt),
                        errorMessage = null,
                        categoryDocumentId = data.documentId.toInt(),
                    )
                }
                Utils.PrefsUtil.saveDocumentId(appContext, data.documentId.toInt())
                _screenState.value = UiScreenState.Success
            }
            is ApiResultV2.SessionExpired -> sessionManager.expireSession()
            is ApiResultV2.ServerError,
            is ApiResultV2.NetworkError,
            is ApiResultV2.UnknownError -> {
                val message = result.uiMessage
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                _screenState.value = UiScreenState.Error(message)
            }
        }
    }

    /**
     * SSE TitleReceived 후 퀴즈 세트를 미리 조회해 서버 캐시를 예열한다. (isQuizLoading 토글은 호출부가 담당)
     * 서버가 퀴즈를 아직 다 만들지 못해 빈 리스트를 반환하면, 0.5초 뒤 다시 조회한다.
     * 이 동안 호출부의 finally 가 실행되지 않으므로 버튼은 계속 비활성 상태로 유지된다.
     */
    private suspend fun prefetchQuiz(documentId: Int, documentType: GoalTypeUiState) {
        Log.d("SummaryViewModel", "퀴즈 프리페치 요청: documentId=$documentId, documentType=$documentType")
        while (true) {
            when (val result = quizRepository.getUserQuizzes(documentId, documentType)) {
                is ApiResultV2.Success -> {
                    if (result.data.isNotEmpty()) {
                        Log.d("SummaryViewModel", "퀴즈 프리페치 완료: documentId=$documentId")
                        return
                    }
                    Log.d("SummaryViewModel", "퀴즈 프리페치 결과 없음 → 0.5초 후 재시도: documentId=$documentId")
                    delay(500)
                }
                else -> {
                    Log.w("SummaryViewModel", "퀴즈 프리페치 실패: documentId=$documentId (QuizActivity에서 재요청)")
                    return
                }
            }
        }
    }

}

