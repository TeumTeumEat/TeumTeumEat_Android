package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.datastore.QuizTrackingDataStore
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.domain.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class QuizViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    private val goalRepository: GoalRepository,
    private val historyRepository: HistoryRepository,
    private val quizTrackingDataStore: QuizTrackingDataStore,
    private val analyticsLogger: TeumAnalyticsLogger,
    val sessionManager: SessionManager,
) : ViewModel() {

    // Intent로 전달된 값을 가져옵니다. (SummaryActivity에서 넣은 Key와 일치해야 함)
// 1. 안전하게 데이터를 꺼내와서 Enum으로 변환
    val goalType: GoalTypeUiState = GoalTypeUiState.fromString(
        savedStateHandle.get<String>("goalType")
    )
    val documentId: Long = savedStateHandle.get<Long>("documentId") ?: -1L
    val topic: String = savedStateHandle.get<String>("topic") ?: ""

    /**
     * 퀴즈 화면 진입 전 user-quizzes/status 응답의 전역 hasSolvedToday.
     * complete-set 성공 후 재조회 값과 비교해 stamp_earned 변경 감지에 사용한다.
     * ⚠️ complete-set 성공 후 값이 바뀌므로 반드시 진입 전 값을 그대로 유지해야 한다.
     */
    private val hasSolvedTodayBefore: Boolean =
        savedStateHandle.get<Boolean>(QuizActivity.EXTRA_HAS_SOLVED_TODAY) ?: false

    /** ViewModel 인스턴스당 quiz_start 이벤트 중복 발송 방지 플래그 */
    private var hasQuizStartLogged = false

    /** [logQuizStartIfNeeded] 동시 호출 시 중복 발송 방지용 (check-then-act 레이스 방지) */
    private val quizStartLogMutex = Mutex()

    /** 이 ViewModel 인스턴스(=1회 퀴즈 세션) 내 성공 제출 수. quiz_abandoned 판정 전용 — 전역 누적값과 분리 */
    private var sessionAnsweredCount = 0

    /** 이 ViewModel 인스턴스(=1회 퀴즈 세션) 내 정답 제출 수. quiz_complete의 correct_count로 사용 */
    private var correctCount = 0

    /**
     * quiz_start 발화 시 계산된 entry_type. quiz_abandoned·quiz_complete에서 재사용하기 위해 필드로 보관.
     * QuizResultActivity로의 Intent extra 전달을 위해 외부에서 읽을 수 있도록 공개(읽기 전용)한다.
     */
    var resolvedEntryType: String = "first"
        private set

    /**
     * quiz_start 발화 시 조회된 사용자 난이도. quiz_complete에서 재사용하기 위해 필드로 보관한다.
     * difficulty 조회 실패로 quiz_start 자체가 스킵되면 Difficulty.NONE으로 남고,
     * logQuizComplete 내부의 동일한 가드에 의해 quiz_complete도 함께 스킵된다.
     */
    private var resolvedDifficulty: Difficulty = Difficulty.NONE

    private val _uiState = MutableStateFlow(UiStateQuiz())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    fun completeQuiz() {
        // ✅ 2️⃣ 전역 시그널 방출
        // Repository 내부의 MutableSharedFlow에 신호를 보냅니다.
        // 이 신호는 MainActivity 등에서 감지하여 데이터를 새로고침하게 됩니다.
        viewModelScope.launch {
            completeCurrentQuizSet()
            goalRepository.emitRefreshSignal()
        }
    }

    /**
     * 퀴즈 완료를 API 호출 시 - 유저 쿠폰수 차감 및 퀴즈 풀이 횟수 1증가 API 호출됨
     * 성공 시 QUIZ-004 quiz_complete 이벤트를 로깅하고, STAMP-001 stamp_earned 변경 감지를 시도한다.
     */
    private fun completeCurrentQuizSet() {
        viewModelScope.launch {
            when (val response = quizRepository.submitCompleteQuizSet()) {
                is ApiResultV2.Success -> {
                    quizTrackingDataStore.markQuizCompleted(documentId.toString())

                    val quizCount = uiState.value.totalSteps
                    val scoreRate = if (quizCount > 0) {
                        (correctCount.toFloat() / quizCount.toFloat() * 100)
                    } else {
                        0f
                    }
                    analyticsLogger.logQuizComplete(
                        contentId = documentId.toString(),
                        topic = topic,
                        difficulty = resolvedDifficulty,
                        entryType = resolvedEntryType,
                        quizCount = quizCount.toLong(),
                        correctCount = correctCount.toLong(),
                        scoreRate = scoreRate.toString(),
                    )

                    logStampEarnedIfSolvedTodayChanged()
                }
                else -> { moveToError(response) }
            }
        }
    }

    /**
     * STAMP-001 — hasSolvedToday 변경 감지(false → true)로 "오늘 하루 첫 완료"를 판정해
     * stamp_earned 이벤트를 로깅한다.
     *
     * 오늘 이미 완료 후 재도전(hasSolvedTodayBefore == true)인 경우 스킵하며,
     * status/calendar 재조회 실패 시에도 조용히 스킵한다(quiz_complete에는 영향 없음).
     */
    private suspend fun logStampEarnedIfSolvedTodayChanged() {
        if (hasSolvedTodayBefore) return

        val hasSolvedTodayAfter = when (val result = quizRepository.getUserQuizStatus()) {
            is ApiResultV2.Success -> result.data.hasSolvedToday
            else -> return
        }
        if (!hasSolvedTodayAfter) return

        val now = LocalDate.now()
        when (
            val calendarResult =
                historyRepository.getCalendarHistory(year = now.year, month = now.monthValue)
        ) {
            is ApiResultV2.Success -> {
                val calendar = calendarResult.data
                analyticsLogger.logStampEarned(
                    contentId = documentId.toString(),
                    streakCount = calendar.currentStreak.toLong(),
                    totalStamps = calendar.totalStamps.toLong(),
                    monthlyStamps = calendar.monthlyStamps.toLong(),
                )
            }
            else -> return
        }
    }

    fun prevQuiz() {
        _uiState.update { state ->
            if (state.currentIndex <= 0) {
                // 첫 페이지일 때 팝업 상태를 true로 변경
                state.copy(showExitDialog = true)
            } else {
                state.copy(
                    currentIndex = state.currentIndex - 1
                )
            }
        }
    }

    // 팝업 닫기 기능 (취소 버튼용)
    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
    }

    private fun moveToNextQuizIfPossible(isCorrect: Boolean) {

        _uiState.update { state ->
            val isLastQuiz = state.currentIndex == state.quizzes.lastIndex

            if (isLastQuiz) {
                state.copy(isCompleted = true)
            } else {
                // 다음 문제로 이동
                state.copy(currentIndex = state.currentIndex + 1)
            }
        }
    }

    fun resetIdleState() {
        _screenState.value = UiScreenState.Idle
    }


    fun loadQuizzes(documentId: Long, goalType: GoalTypeUiState) {

        viewModelScope.launch {
            _screenState.value = UiScreenState.Loading
            _uiState.update{
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            when (
                val result =
                    quizRepository.getUserQuizzes(documentId.toInt(), goalType)
            ) {

                is ApiResultV2.Success -> {
                    // 🔍 1. Domain 단계 quizId 확인
                    result.data.forEachIndexed { index, quiz ->
                        Log.d(
                            "QuizDebug",
                            "Domain[$index] quizId=${quiz.quizId}, question=${quiz.question}"
                        )
                    }

                    _uiState.update {
                        val uiQuizzes = result.data.map { quiz ->
                            val ui = quiz.toUiState()

                            // 🔍 2. UiState 단계 quizId 확인
                            Log.d(
                                "QuizDebug",
                                "UiState quizId=${ui.quizId}, question=${ui.question}"
                            )

                            ui
                        }

                        it.copy(
                            isLoading = false,
                            quizzes = uiQuizzes,
                            currentIndex = 0
                        )
                    }

                    _screenState.value = UiScreenState.Success
                    logQuizStartIfNeeded(quizCount = result.data.size)
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
                        UiScreenState.Error(result.uiMessage)
                }
            }
        }
    }

    /**
     * QUIZ-001 — 퀴즈 화면 진입 이벤트 로깅. ViewModel 인스턴스당 최초 1회만 발송한다.
     * 사용자 난이도 조회 실패 시 조용히 스킵한다 (퀴즈 플로우 자체는 막지 않음).
     */
    private suspend fun logQuizStartIfNeeded(quizCount: Int) {
        if (hasQuizStartLogged) return

        quizStartLogMutex.withLock {
            if (hasQuizStartLogged) return@withLock

            val difficulty = when (val goalResult = goalRepository.getUserGoal()) {
                is ApiResultV2.Success -> goalResult.data.difficulty
                else -> return@withLock
            }
            resolvedDifficulty = difficulty
            val entryType = quizTrackingDataStore.resolveEntryType(documentId.toString())
            resolvedEntryType = entryType

            hasQuizStartLogged = true
            analyticsLogger.logQuizStart(
                contentId = documentId.toString(),
                topic = topic,
                quizCount = quizCount.toLong(),
                difficulty = difficulty,
                entryType = entryType,
            )
        }
    }

    fun submitAnswer(answer: String) {
        // todo. answer 값이 비어있으면 정답에 값 선택안됨 처리
        val state = uiState.value
        val quiz = state.currentQuiz ?: return

        Log.d(
            "QuizViewModel",
            "submit quizId=${quiz?.quizId}, index=${state.currentIndex}"
        )

        viewModelScope.launch {

            // 1️⃣ 카드 submitting 처리
            _uiState.update {
                it.copy(
                    quizzes = it.quizzes.mapIndexed { index, q ->
                        if (index == it.currentIndex)
                            q.copy(
                                selectedAnswer = answer,
                                isSubmitting = true
                            )
                        else q
                    }
                )
            }

            // 2️⃣ 제출 API 호출
            Log.d("QuizViewModel", "제출 api 호출")
            when (
                val result = quizRepository.submitQuiz(
                    quizId = quiz.quizId,
                    userAnswer = answer
                )
            ) {
                is ApiResultV2.Success -> {
                    val isCorrect = result.data.isCorrect

                    _uiState.update { state ->
                        val updatedQuizzes =
                            state.quizzes.mapIndexed { index, q ->
                                if (index == state.currentIndex)
                                    q.copy(
                                        isSubmitting = false,
                                        isSubmitted = true,
                                        isCorrect = isCorrect
                                    )
                                else q
                            }

                        state.copy(
                            quizzes = updatedQuizzes,
                        )
                    }

                    val questionNo = quizTrackingDataStore.incrementAndGetTotalQuestionsAnswered()
                    sessionAnsweredCount++
                    if (isCorrect) correctCount++
                    analyticsLogger.logQuizAnswerSubmit(
                        contentId = documentId.toString(),
                        questionNo = questionNo.toLong(),
                        answerType = quiz.type.toAnalyticsValue(),
                        isCorrect = isCorrect,
                    )

                    // ✅ 결과 반영 후 "이동 여부"는 여기서 판단
                    moveToNextQuizIfPossible(isCorrect)
                }

                else -> {
                    val message = result.uiMessage

                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = message)
                    }
                    _screenState.value =
                        UiScreenState.Error(message)
                }
            }
        }
    }

    private suspend fun moveToError(result: ApiResultV2<*>) {
        when (result) {
            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
            }

            is ApiResultV2.NetworkError -> {
                _uiState.update {
                    it.copy(
                        errorMessage = result.uiMessage
                    )
                }
            }

            is ApiResultV2.ServerError -> {
                _uiState.update {
                    it.copy(
                        errorMessage = result.uiMessage
                    )
                }
            }

            else -> {

                _uiState.update {
                    it.copy(
                        errorMessage = "알 수 없는 오류가 발생했습니다."
                    )
                }
            }
        }

    }

    /**
     * QUIZ-003 — 화면 이탈 감지. QuizActivity가 finish()될 때 ViewModel도 clear되므로
     * 이 시점에서 세션 내 제출 수가 전체 문항 수보다 적으면 미완료 이탈로 간주한다.
     */
    override fun onCleared() {
        super.onCleared()
        val quizCount = uiState.value.totalSteps
        if (sessionAnsweredCount < quizCount) {
            analyticsLogger.logQuizAbandoned(
                contentId = documentId.toString(),
                lastQuestionNo = sessionAnsweredCount.toLong(),
                quizCount = quizCount.toLong(),
                entryType = resolvedEntryType,
            )
        }
    }
}
