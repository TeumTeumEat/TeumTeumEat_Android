package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState.*
import com.teumteumeat.teumteumeat.utils.date_change_reciver.DateChangeReceiver
import com.teumteumeat.teumteumeat.utils.manager.ad.InterstitialAdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val quizRepository: QuizRepository,
    val sessionManager: SessionManager,
    private val dateChangeReceiver: DateChangeReceiver,
    @ApplicationContext private val context: Context, // Context 주입 필요
    private val adManager: InterstitialAdManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateHome())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    // 서버에서 받은 goal 캐싱 (SnackState 계산용)
    private var cachedGoal: UserGoal? = null

    init {
        // 실제 앱 구동 시에만 리시버 등록
        // 만료된 목표일 때 만
        // setupDateChangeReceiver()

        // 앱 시작 후 메인 액티비티 진입 시 광고 로드
        observeAdStatus()

        loadHomeState()
        // 2. 목표 변경 리프래시 시그널 감지
        viewModelScope.launch {
            // todo. 목표를 완료하고 돌아왔을 때도 loadHomeState() 호출
            goalRepository.refreshSignal.collect {
                // 다른 액티비티에서 목표를 변경하고 돌아왔을 때 호출됨
                loadHomeState()
            }
        }
    }

    private fun observeAdStatus() {
        viewModelScope.launch {
            // 광고 상태를 관찰하여 null이 되면 자동으로 로드
            adManager.interstitialAd.collect { ad ->
                if (ad == null) {
                    adManager.loadAd()
                }
            }
        }
    }

    fun submitAdWatching() {
        viewModelScope.launch {
            when (val adRewardResponse = quizRepository.getAdReward()) {
                is ApiResultV2.Success -> {
                    updateUserQuizStatus()
                }
                else -> { moveToError(adRewardResponse) }
            }
        }
    }

    private fun updateUserQuizStatus() {
        viewModelScope.launch {
            when (val response = quizRepository.getUserQuizStatus()) {
                is ApiResultV2.Success -> {
                    // 유저 퀴즈 상태 재조회 후 바뀐 값으로 리랜더링
                    val quizStatus = response.data
                    _uiState.update { currentState ->
                        currentState.copy(
                            isShowAdModalDialog = true,
                            cupponCount = quizStatus.availableQuizCount // 서버에서 받아온 새 개수
                        )
                    }
                }
                else -> { moveToError(response) }
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

    fun showInterstitialAdWithLoading(activity: Activity, onAdDismissed: () -> Unit) {
        val currentAd = adManager.interstitialAd.value

        if (currentAd != null) {
            // 1. 이미 광고가 있는 경우 즉시 노출
            showAd(currentAd, activity, onAdDismissed)
        } else {
            // 2. 광고가 없는 경우 로딩 시작 및 로드 대기
            _uiState.update { it.copy(isAdLoading = true) }
            adManager.loadAd() // 광고 로드 요청

            viewModelScope.launch {
                // 광고가 로드될 때까지(null이 아닐 때까지) 기다림
                adManager.interstitialAd.collect { ad ->
                    if (ad != null) {
                        _uiState.update { it.copy(isAdLoading = false) }
                        showAd(ad, activity, onAdDismissed)
                        this.cancel()
                    }
                }

            }
        }
    }

    private fun showAd(ad: InterstitialAd, activity: Activity, onAdDismissed: () -> Unit) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // 사용자가 광고를 끝까지 보거나 중간에 닫았을 때 호출됨
                adManager.clearAd()
                onAdDismissed() // 여기서 다음 화면 이동 로직 실행
                // 로직 처리 후 쿠폰 수 리렌더링
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                adManager.clearAd()
                // todo. 광고 노출 실패 알림
                // onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                // 광고가 화면에 나타났으므로 로딩 해제
                _uiState.update { it.copy(isAdLoading = false) }
            }
        }
        ad.show(activity)
    }

    internal fun setupDateChangeReceiver() {

        dateChangeReceiver.setOnDateChangedListener {
            loadHomeState() // 날짜 변경 시 실행할 비즈니스 로직
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)

            // 디버그 모드일 때만 테스트용 커스텀 액션 추가
            if (BuildConfig.DEBUG) {
                addAction("com.teumteumeat.test.ACTION_DATE_CHANGED")
            }
        }

        // 모드에 따른 보안 플래그 설정
        val flags = if (BuildConfig.DEBUG) {
            ContextCompat.RECEIVER_EXPORTED // 디버그: ADB 등 외부 신호 허용
        } else {
            ContextCompat.RECEIVER_NOT_EXPORTED // 릴리즈: 외부 앱/ADB 차단 (보안 강화)
        }

        // ContextCompat을 사용하여 등록
        ContextCompat.registerReceiver(
            context,
            dateChangeReceiver,
            filter,
            flags
        )

        if (BuildConfig.DEBUG) {
            Log.d("HomeViewModel", "리시버 등록 완료 (디버그 모드 - 외부 노출 허용)")
        }
    }

    // 모달 열기
    fun openAdModal() {
        _uiState.update { it.copy(isShowAdModalDialog = true) }
    }

    // 모달 닫기
    fun closeAdModal() {
        _uiState.update { it.copy(isShowAdModalDialog = false) }
    }

    // 테스트에서 감시(Spy)하기 위해 open 또는 internal로 선언
    internal fun onDateChangedTriggered() {
        loadHomeState()
    }

    fun setRandomFood() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedFoodRes = currentState.foodList.random()
            )
        }
    }

    /**
     * 홈 진입 시 서버 기준 상태 로딩
     */
    fun loadHomeState() {
        viewModelScope.launch {
            _screenState.value = UiScreenState.Loading

            Log.d("요약글 조회 디버깅", "홈화면 상태 가져옴 - 목표 조회 완료")

            setRandomFood()

            // 1️⃣ 목표 조회
            when (val goalResult = goalRepository.getUserGoal()) {

                is ApiResultV2.Success -> {
                    val goal = goalResult.data
                    cachedGoal = goal
                    Log.d("user's current goal", "${goal}")

                    // 2️⃣ 오늘 퀴즈 상태 조회
                    when (val quizResult = quizRepository.getUserQuizStatus()) {

                        is ApiResultV2.Success -> {
                            val quizStatus = quizResult.data

                            _uiState.update {
                                it.copy(
                                    fireState = resolveFireState(goal),

                                    // 🔥 서버 기준 값 저장
                                    hasSolvedToday = quizStatus.hasSolvedToday,
                                    hasCreatedToday = quizStatus.hasCreatedToday,
                                    isFirstTime = quizStatus.isFirstTime,
                                    cupponCount = quizStatus.availableQuizCount,

                                    // 🔥 HomeViewModel에서만 SnackState 분기
                                    snackState = resolveSnackState(
                                        goal = goal,
                                        hasSolvedToday = quizStatus.hasSolvedToday,
                                    ),

                                    summaryQuery = buildSummaryQuery(goal)
                                )
                            }
                            // update 호출 직후 확인
                            Log.d(
                                "디버깅_업데이트",
                                "4. 업데이트 완료 후 실제 State 값: ${_uiState.value.summaryQuery}"
                            )
                            _screenState.value = UiScreenState.Success
                        }

                        is ApiResultV2.SessionExpired -> {
                            sessionManager.expireSession()
                        }

                        is ApiResultV2.ServerError,
                        is ApiResultV2.NetworkError,
                        is ApiResultV2.UnknownError -> {
                            _screenState.value = Error(quizResult.uiMessage)
                        }

                    }
                }

                is ApiResultV2.SessionExpired -> {
                    sessionManager.expireSession()
                }

                is ApiResultV2.ServerError,
                is ApiResultV2.NetworkError,
                is ApiResultV2.UnknownError -> {
                    _screenState.value = Error(goalResult.uiMessage)
                }
            }

            if (checkExpiredGoal()) {
                _uiState.update {
                    it.copy(
                        isShowGoalExpiredDialog = true
                    )
                }
            }
        }
    }

    // ================= 홈 비즈니스 로직 =================

    fun checkExpiredGoal(): Boolean {
        val goal = cachedGoal ?: return false
        return goal.isExpired
    }

    /**
     * 만료된 목표 확인 다이얼로그를 닫는 함수
     */
    fun dismissGoalExpiredDialog() {
        _uiState.update {
            it.copy(isShowGoalExpiredDialog = false)
        }
    }

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
        // 빌드 타입이 DEBUG가 아니고(Release), 오늘 이미 해결했다면 Consumed 상태 반환
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

    private suspend fun buildSummaryQuery(goal: UserGoal): SummaryQuery =
        SummaryQuery(
            goalId = goal.goalId,
            goalType = goal.type,
            documentId = goal.documentId,
            categoryId = goal.category?.categoryId
        )

    /* ================= 이벤트 ================= */

    /**
     * ViewModel이 파괴될 때 리시버 등록 해제 (메모리 누수 방지)
     */
    override fun onCleared() {
        super.onCleared()
        /*try {
            context.unregisterReceiver(dateChangeReceiver)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Receiver unregister error", e)
        }*/
    }

}