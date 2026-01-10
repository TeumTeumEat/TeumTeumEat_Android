package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.localdata.preference.HomePreference
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val quizRepository: QuizRepository,
    private val homePreference: HomePreference,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateHome())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    // 서버에서 받은 goal 캐싱 (SnackState 계산용)
    private var cachedGoal: UserGoal? = null

    init {
        loadHomeState()
    }

    /**
     * 홈 진입 시 유저 상태 조회
     */
    /**
     * 홈 진입 시 서버 기준 상태 로딩
     */
    private fun loadHomeState() {
        viewModelScope.launch {
            _screenState.value = UiScreenState.Loading

            // 1️⃣ 목표 조회
            when (val goalResult = goalRepository.getUserGoal()) {

                is ApiResultV2.Success -> {
                    val goal = goalResult.data
                    cachedGoal = goal

                    // 2️⃣ 오늘 퀴즈 상태 조회
                    when (val quizResult = quizRepository.getUserQuizStatus()) {

                        is ApiResultV2.Success -> {
                            val quizStatus = quizResult.data

                            _uiState.update {
                                it.copy(
                                    fireState = resolveFireState(goal),

                                    // 🔥 서버 기준 값 저장
                                    hasSolvedToday = quizStatus.hasSolvedToday,
                                    isFirstTime = quizStatus.isFirstTime,

                                    // 🔥 HomeViewModel에서만 SnackState 분기
                                    snackState = resolveSnackState(
                                        goal = goal,
                                        hasSolvedToday = quizStatus.hasSolvedToday
                                    ),

                                    summaryQuery = buildSummaryQuery(goal)
                                )
                            }

                            _screenState.value = UiScreenState.Success
                        }

                        is ApiResultV2.ServerError,
                        is ApiResultV2.NetworkError,
                        is ApiResultV2.SessionExpired,
                        is ApiResultV2.UnknownError -> {
                            _screenState.value =
                                UiScreenState.Error(quizResult.uiMessage)
                        }
                    }
                }

                is ApiResultV2.ServerError,
                is ApiResultV2.NetworkError,
                is ApiResultV2.SessionExpired,
                is ApiResultV2.UnknownError -> {
                    _screenState.value =
                        UiScreenState.Error(goalResult.uiMessage)
                }
            }
        }
    }

    // ================= 홈 비즈니스 로직 =================

    /* ================= 상태 계산 ================= */

    private fun resolveFireState(goal: UserGoal): FireState =
        if (goal.isExpired) FireState.UnBurning else FireState.Burning


    /**
     * 🔥 햄버거(Snack) 상태의 단일 결정 함수
     */
    private fun resolveSnackState(
        goal: UserGoal,
        hasSolvedToday: Boolean
    ): SnackState {

        // 1️⃣ 목표 기간 종료
        if (goal.isExpired) {
            return SnackState.Expired
        }

        // 2️⃣ 오늘 이미 소비
        if (hasSolvedToday) {
            return SnackState.Consumed(
                nextArrivalTime = "00:00"
            )
        }

        // 3️⃣ 사용 가능
        return SnackState.Available
    }

    private fun calculateStampCount(goal: UserGoal): Int =
        if (goal.isExpired) 0 else 1

    private fun buildSummaryQuery(goal: UserGoal): SummaryQuery =
        SummaryQuery(
            goalId = goal.goalId,
            goalType = goal.type,
            documentId = null,
            categoryId = goal.category?.categoryId
        )

    /* ================= 이벤트 ================= */

}