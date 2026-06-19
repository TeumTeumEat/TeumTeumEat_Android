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
import com.teumteumeat.teumteumeat.domain.model.sse.DocumentProcessingEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseBusinessException
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.document.StreamDocumentProcessingUseCase
import com.teumteumeat.teumteumeat.domain.usecase.summary.StreamDailySummaryUseCase
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ProcessingUiState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.TimeUtil.toMonthDay
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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


@dagger.hilt.android.lifecycle.HiltViewModel
class SummaryViewModel @Inject constructor(
    private val pdfDocumentRepository: PdfDocumentRepository,
    private val categoryRepository: CategoryRepository,
    private val quizRepository: QuizRepository,
    private val streamDocumentProcessingUseCase: StreamDocumentProcessingUseCase,
    private val streamDailySummaryUseCase: StreamDailySummaryUseCase,
    val application: Application,
    val sessionManager: SessionManager,
) : ViewModel() {

    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(UiStateSummary())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private val _processingState =
        MutableStateFlow<ProcessingUiState?>(null)
    val processingState = _processingState.asStateFlow()
    private var processingJob: Job? = null

    private val _event = MutableSharedFlow<UiEvent>()
    val event: SharedFlow<UiEvent> = _event

    /** SSE 스트리밍으로 요약글을 생성해야 하는 경우 true. initSummary 호출 시 설정된다. */
    private var shouldForceStream = false

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _screenState.value = UiScreenState.Loading

            val job1 = launch { loadUserQuizStatus() }
            val job2 = launch { loadSummaryByGoalType() }

            job1.join()
            job2.join()

            // SSE 스트리밍 경로는 startCategorySummaryStream / loadDocumentSummary 가 자체 관리한다.
            // GET 경로(forceStream=false)에서만 여기서 Success로 전환한다.
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
                        if (shouldForceStream) {
                            // 문서 처리 대기 중 → SSE로 처리 완료 감지 후 요약글 로드
                            loadDocumentSummary(currentState.goalId.toInt(), currentState.documentId.toInt())
                        } else {
                            // 문서 처리 완료, 기존 요약글 직접 조회
                            _screenState.value = UiScreenState.Loading
                            fetchDocumentSummary(currentState.goalId.toInt(), currentState.documentId.toInt())
                        }
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

        // CATEGORY는 항상 SSE로 생성 시도. DOCUMENT는 caller가 전달한 forceStream 값 사용.
        shouldForceStream = if (goalType == DomainGoalType.CATEGORY) true else forceStream
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
                        viewModelScope.launch {
                            // 요약 GET·퀴즈 프리페치가 중단되어도 버튼이 "퀴즈를 불러오는 중..."에
                            // 고정되지 않도록 finally 에서 isQuizLoading 을 반드시 내린다.
                            try {
                                loadCategorySummaryInternal(categoryId.toInt())
                                val documentId = _uiState.value.categoryDocumentId
                                if (documentId != -1) prefetchCategoryQuiz(documentId)
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
     * 문서 처리 상태를 SSE 로 스트리밍하여 요약글을 로드한다.
     *
     * PENDING/PROCESSING → 로딩 바 표시 → COMPLETED → 요약글 API 호출
     * FAILED / StreamError → 에러 화면
     */
    fun loadDocumentSummary(goalId: Int, documentId: Int) {
        processingJob?.cancel()

        viewModelScope.launch {
            _screenState.value = UiScreenState.Loading
            _processingState.value = null

            if (goalId == -1 || documentId == -1) {
                _screenState.value = UiScreenState.Error("goalId 나 documentId 가 등록되지 않았습니다.")
                return@launch
            }

            streamDocumentProcessingUseCase(goalId.toLong(), documentId.toLong())
                .collect { event ->
                    when (event) {
                        is DocumentProcessingEvent.Connected,
                        is DocumentProcessingEvent.Pending -> {
                            _processingState.value = null
                        }

                        is DocumentProcessingEvent.Processing -> {
                            startProgressAnimation(event.remainMs)
                        }

                        is DocumentProcessingEvent.Completed -> {
                            processingJob?.cancel()
                            _processingState.value = ProcessingUiState(progress = 1f)
                            fetchDocumentSummary(goalId, documentId)
                        }

                        is DocumentProcessingEvent.Failed -> {
                            processingJob?.cancel()
                            _screenState.value = UiScreenState.Error(event.reason.toUiMessage())
                        }

                        is DocumentProcessingEvent.StreamError -> {
                            processingJob?.cancel()
                            _screenState.value = UiScreenState.Error(
                                event.throwable.message ?: "알 수 없는 오류가 발생했습니다."
                            )
                        }
                    }
                }
        }
    }

    /** remain_ms 기반으로 0f → 0.99f 진행 애니메이션을 별도 Job 으로 구동한다. */
    private fun startProgressAnimation(remainMs: Long) {
        processingJob?.cancel()
        val startTime = System.currentTimeMillis()
        val duration = remainMs.coerceAtLeast(1_000L)

        processingJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed.toFloat() / duration).coerceIn(0f, 0.99f)
                _processingState.value = ProcessingUiState(progress = progress)
                if (elapsed >= duration) break
                delay(100L)
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

    /** SSE TitleReceived 후 퀴즈를 미리 생성 요청한다. (isQuizLoading 토글은 호출부 finally 가 담당) */
    private suspend fun prefetchCategoryQuiz(documentId: Int) {
        Log.d("SummaryViewModel", "퀴즈 생성 요청: documentId=$documentId")
        when (quizRepository.getUserQuizzes(documentId, GoalTypeUiState.CATEGORY)) {
            is ApiResultV2.Success -> Log.d("SummaryViewModel", "퀴즈 생성 완료: documentId=$documentId")
            else -> Log.w("SummaryViewModel", "퀴즈 생성 실패: documentId=$documentId (QuizActivity에서 재요청)")
        }
    }

}

private fun DocumentProcessingEvent.FailureReason.toUiMessage(): String = when (this) {
    DocumentProcessingEvent.FailureReason.TIMEOUT        -> "문서 처리 시간이 초과되었습니다. 문서를 다시 등록해주세요."
    DocumentProcessingEvent.FailureReason.SERVER_ERROR   -> "서버 오류로 문서 처리에 실패했습니다. 잠시 후 다시 시도해주세요."
    DocumentProcessingEvent.FailureReason.ENCRYPTED_FILE -> "암호화된 파일은 처리할 수 없습니다. 다른 파일을 등록해주세요."
    DocumentProcessingEvent.FailureReason.UNKNOWN        -> "문서 처리에 실패했습니다. 문서를 다시 등록해주세요."
}