package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import android.app.Application
import android.se.omapi.Session
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_response.DocumentResponse
import com.teumteumeat.teumteumeat.data.network.model_response.DocumentStatus
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.quiz.UserQuizStatus
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ProcessingUiState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.TimeUtil.toMonthDay
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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
    data class ShowError(val message: String) : UiEvent
}


@dagger.hilt.android.lifecycle.HiltViewModel
class SummaryViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val categoryRepository: CategoryRepository,
    private val quizRepository: QuizRepository,
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


    fun loadInitialData() {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }
            // 🔵 로딩 시작
            _screenState.value = UiScreenState.Loading

            // 🔹 동시에 실행할 작업들
            launch {
                loadUserQuizStatus()
            }

            // 🔹 2) 내부에서 상태를 처리하는 작업 → launch
            launch {
                loadSummaryByGoalType()
            }

            _screenState.value = UiScreenState.Success
        }
    }

    private suspend fun handleQuizStatusResult(
        result: ApiResultV2<UserQuizStatus>
    ) {
        when (result) {
            is ApiResultV2.Success -> {
                _uiState.update {
                    it.copy(isQuizGuideSeen = result.data.isQuizGuideSeen)
                }
            }

            else -> {
                _event.emit(
                    UiEvent.ShowError(result.uiMessage)
                )
            }
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
                // 공통 에러 메시지 처리
                _event.emit(
                    UiEvent.ShowError(result.uiMessage)
                )
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
        val state = _uiState.value

        val goalType = state.goalType
        val goalId = state.goalId
        val documentId = state.documentId
        val categoryId = state.categoryId

        if (goalType == null || goalId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "요약글을 불러오기 위한 정보가 부족합니다."
                )
            }
            _screenState.value =
                UiScreenState.Error("요약글을 불러오기 위한 정보가 부족합니다.")
            return
        }

        when (goalType) {

            DomainGoalType.DOCUMENT -> {
                if (documentId == null) {
                    handleInvalidParam("documentId 가 없습니다.")
                    return
                }
                loadDocumentSummary(
                    goalId = goalId.toInt(),
                    documentId = documentId.toInt()
                )
            }

            DomainGoalType.CATEGORY -> {
                if (categoryId == null) {
                    handleInvalidParam("category - documentId 가 없습니다.")
                    return
                }
                loadCategorySummary(
                    categoryId = categoryId.toInt()
                )
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
        documentId: Long?,
        categoryId: Long?
    ) {
        // 이미 초기화되었으면 재실행 방지
        if (_uiState.value.goalType != null) return

        _uiState.update {
            it.copy(
                goalId = goalId,
                goalType = goalType,
                documentId = documentId,
                categoryId = categoryId
            )
        }

        // goalType 기준으로 API 분기
        when (goalType) {
            DomainGoalType.CATEGORY -> {
                loadCategorySummary(categoryId?.toInt() ?: -1)
            }

            DomainGoalType.DOCUMENT -> {
                loadDocumentSummary(
                    goalId = goalId.toInt(),
                    documentId = documentId?.toInt() ?: -1
                )
            }
        }
    }

    /**
     * 오늘의 남남지식 요약 조회
     */
    fun loadDocumentSummary(goalId: Int, documentId: Int) {
        viewModelScope.launch {
            _screenState.value = UiScreenState.Loading

            if (goalId == -1 || documentId == -1){
                _screenState.value = UiScreenState.Error("goalId 나 documentId 가 등록되지 않았습니다.")
            }

/*

            // todo.
            //  0. status 의 상태에 따라서 분기 처리
            //      #0"PENDING" - 대기 중
            //          - 3초 후 재요청
            //      #1"PROCESSING"
            //          - estimateTime(ms단위) 이 끝나면 재요청
            //          - 아래 result 값의 문서 배열의 documentId 아이템의 estimateTime 에 따라 로딩바 표시
            //          - 0 이라면 1초 뒤 재요청
            //      #2"COMPLETED" - 재요청 하여 결과 document 표시
            //      #3"FAILED" - 처리 실패 상태
            //          - 문서 재등록 안내, 적절한 실패 사유 안내 띄워주기
            //          - 문서(요약글, 퀴즈) 재생성 api 요청(백앤드 API 구현시 구현 예정)
            // 1️⃣ 문서 상태 조회
            val isCompleted = when (
                val result = documentRepository.getDocuments(goalId)
            ) {
                is ApiResultV2.Success -> {
                    handleDocumentsResult(
                        goalId = goalId,
                        documentId = documentId,
                        documents = result.data
                    )
                }

                else -> {
                    _screenState.value =
                        UiScreenState.Error("문서 조회에 실패하였습니다.")
                    false
                }
            }
            if (!isCompleted) return@launch
*/

// 🔥 기존 애니메이션 중단
            processingJob?.cancel()

            // 1️⃣ 로딩 애니메이션 시작 (무한 반복)
            processingJob = launch {

                val totalSteps = 10

                while (isActive) {

                    for (step in 0..totalSteps) {

                        val progress =
                            (step.toFloat() / totalSteps.toFloat())
                                .coerceIn(0f, 1f)

                        _processingState.value =
                            ProcessingUiState(progress = progress)

                        delay(1_000L)
                    }

                    // ⭐ 100% 후 자동으로 0%로 돌아감 (loop 재시작)
                }
            }

            // 2️⃣ Summary API 호출
            val result = async {
                documentRepository.getDocumentSummary(goalId, documentId)
            }.await()

            // 3️⃣ 응답 도착 → 애니메이션 중단
            processingJob?.cancel()

            // ⭐ UX 보정: 완료 시 100% 유지
            _processingState.value =
                ProcessingUiState(progress = 1f)

            // 4️⃣ 결과 처리
            when (result) {

                is ApiResultV2.Success -> {
                    val summary = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = summary.fileName,
                            dateText = toMonthDay(summary.updatedAt),
                            summary = summary.summary,
                            hasSolvedToday = summary.hasSolvedToday,
                            isFirstTime = summary.isFirstTime,
                            errorMessage = null
                        )
                    }

                    _screenState.value = UiScreenState.Success
                }

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
                        UiScreenState.Error("문서 조회에 실패하였습니다.")
                }
            }
            /*when (val result = documentRepository.getDocumentSummary(goalId, documentId)) {

                is ApiResultV2.Success -> {
                    val summary = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = summary.fileName,          // 화면 타이틀용
                            dateText = toMonthDay(summary.updatedAt),
                            summary = summary.summary,          // 요약 본문
                            hasSolvedToday = summary.hasSolvedToday,
                            isFirstTime = summary.isFirstTime,
                            errorMessage = null
                        )
                    }
                    _screenState.value = UiScreenState.Success
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.uiMessage
                        )
                    }
                    _screenState.value = UiScreenState.Error("문서 조회에 실패하였습니다.")
                }
            }*/
        }
    }

    private fun handleDocumentsResult(
        goalId: Int,
        documentId: Int,
        documents: List<DocumentResponse>
    ): Boolean {
        val targetDocument = documents
            .firstOrNull { it.documentId == documentId }
            ?: run {
                _screenState.value =
                    UiScreenState.Error("조회한 문서를 찾을 수 없습니다.")
                return false
            }
        Log.d("$this", "targetDocument.status: ${targetDocument.status}, ${targetDocument}")

        return when (targetDocument.status) {
            DocumentStatus.PENDING -> {
                handlePending(goalId, documentId)
                false
            }

            DocumentStatus.PROCESSING -> {
                handleProcessing(goalId, documentId, documents)
                false
            }

            DocumentStatus.COMPLETED -> {
                true // ⭐ 여기서만 다음 단계 진행
            }

            DocumentStatus.FAILED -> {
                _screenState.value =
                    UiScreenState.Error("문서 처리에 실패했습니다. 문서를 다시 등록해주세요.")
                false
            }
        }
    }


    private fun handlePending(
        goalId: Int,
        documentId: Int
    ) {
        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {
            delay(3_000L)
            loadDocumentSummary(goalId, documentId)
        }
    }


    private fun handleProcessing(
        goalId: Int,
        documentId: Int,
        documents: List<DocumentResponse>
    ) {
        val targetDocument = documents
            .firstOrNull { it.documentId == documentId }
            ?: return


        // 🔹 기존 sealed class에 영향 주지 않기 위해 Loading 재사용
        _screenState.value = UiScreenState.Loading

        processingJob?.cancel()

        processingJob = viewModelScope.launch {

            val totalDuration = 10_000L   // 10초
            val pollingInterval = 1_000L  // 1초
            val totalSteps = (totalDuration / pollingInterval).toInt()

            for (step in 0..totalSteps) {

                // 1️⃣ progress 계산 (10초 기준)
                val progress =
                    (step.toFloat() / totalSteps.toFloat())
                        .coerceIn(0f, 1f)

                _processingState.value =
                    ProcessingUiState(progress = progress)

                // 2️⃣ 1초마다 서버 재요청
                val result = documentRepository.getDocuments(goalId)

                if (result is ApiResultV2.Success) {

                    val target = result.data
                        .firstOrNull { it.documentId == documentId }

                    if (target?.status == DocumentStatus.COMPLETED) {
                        // 🔥 즉시 종료
                        loadDocumentSummary(goalId, documentId)
                        return@launch
                    }

                    if (target?.status == DocumentStatus.FAILED) {
                        _screenState.value =
                            UiScreenState.Error("문서 처리에 실패했습니다.")
                        return@launch
                    }
                }

                delay(pollingInterval)
            }

            // 🔁 10초 지나도 완료 안 되면 다시 summary 요청
            loadDocumentSummary(goalId, documentId)
        }

        /*val estimateTime = targetDocument.estimateTime ?: 10_000L

        // 1️⃣ estimateTime == 0 → 1초 뒤 재요청
        if (estimateTime == 0L) {
            _processingState.value =
                ProcessingUiState(progress = 0f)

            viewModelScope.launch {
                delay(1_000L)
                loadDocumentSummary(goalId, documentId)
            }
            return
        }*/

        /*// 2️⃣ estimateTime 기반 로딩바 처리
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            while (true) {
                val elapsed =
                    System.currentTimeMillis() - startTime

                val progress =
                    (elapsed.toFloat() / estimateTime)
                        .coerceIn(0f, 1f)

                _processingState.value =
                    ProcessingUiState(progress = progress)

                if (elapsed >= estimateTime) break

                delay(100L) // UI 업데이트 주기 (부하 방지)
            }

            // 3️⃣ estimateTime 종료 후 재요청
            loadDocumentSummary(goalId, documentId)
        }*/
    }


    fun resetIdleState() {
        _screenState.value = UiScreenState.Idle
    }

    fun loadCategorySummary(categoryId: Int) {
        viewModelScope.launch {


            _uiState.update{
                it.copy(
                    categoryId = categoryId.toLong(),
                    isLoading = true,
                    errorMessage = null,
                )
            }

            if (categoryId == -1){
                _uiState.update {
                    _screenState.value =
                        UiScreenState.Error("categoryId 가 등록되지 않았습니다.")
                    it.copy(
                        isLoading = false,
                        errorMessage = "categoryId 가 등록되지 않았습니다."
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

                is ApiResultV2.SessionExpired -> {
                    sessionManager.expireSession()
                }

                is ApiResultV2.ServerError,
                is ApiResultV2.NetworkError,
                is ApiResultV2.UnknownError -> {
                    val message = result.uiMessage

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = message
                        )
                    }

                    _screenState.value =
                        UiScreenState.Error(message)
                }
            }
        }
    }

}
