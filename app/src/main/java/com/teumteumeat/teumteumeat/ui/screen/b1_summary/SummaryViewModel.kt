package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.quiz.UserQuizStatus
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.TimeUtil.toMonthDay
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
) : ViewModel() {
    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(UiStateSummary())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private val _event = MutableSharedFlow<UiEvent>()
    val event: SharedFlow<UiEvent> = _event


    fun loadInitialData() {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            // 🔹 동시에 실행할 작업들
            launch {
                loadUserQuizStatus()
            }

            // 🔹 2) 내부에서 상태를 처리하는 작업 → launch
            launch {
                loadSummaryByGoalType()
            }

            _uiState.update { it.copy(isLoading = false) }
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
                    handleInvalidParam("categoryId 가 없습니다.")
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


    suspend fun initSummary(
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
            _uiState.update{
                it.copy(
                    isLoading = true,
                    errorMessage = null,
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
                    val summary = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = summary.fileName,          // 화면 타이틀용
                            summary = summary.summary,          // 요약 본문
                            hasSolvedToday = summary.hasSolvedToday,
                            isFirstTime = summary.isFirstTime,
                            errorMessage = null
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

    fun resetIdleState() {
        _screenState.value = UiScreenState.Idle
    }

    fun loadCategorySummary(categoryId: Int) {
        viewModelScope.launch {

            // 🔵 로딩 시작
            _screenState.value = UiScreenState.Loading
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

                is ApiResultV2.ServerError,
                is ApiResultV2.NetworkError,
                is ApiResultV2.SessionExpired,
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
