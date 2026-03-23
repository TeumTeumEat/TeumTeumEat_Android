package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.rewarded.RewardedAd
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
import com.teumteumeat.teumteumeat.utils.manager.ad.RewardedAdManager
import com.teumteumeat.teumteumeat.utils.monitor.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val quizRepository: QuizRepository,
    val sessionManager: SessionManager,
    private val dateChangeReceiver: DateChangeReceiver,
    @ApplicationContext private val context: Context, // Context 주입 필요
    private val adManager: RewardedAdManager,
    private val networkConnection: NetworkConnection,
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

        // ✅ 1. 네트워크 상태 감지 시작
        observeNetworkState()

        // 앱 시작 후 메인 액티비티 진입 시 광고 로드
        observeAdStatus()

        loadHomeState()
        // 2. 목표 변경 리프래시 시그널 감지
        viewModelScope.launch {
            // todo. 목표를 완료하고 돌아왔을 때도 loadHomeState() 호출 되게 변경
            goalRepository.refreshSignal.collect {
                // 다른 액티비티에서 목표를 변경하고 돌아왔을 때 호출됨
                loadHomeState()
            }
        }
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            // 💡 LiveData인 NetworkConnection을 asFlow()를 통해 관찰합니다.
            networkConnection.asFlow().collect { isConnected ->
                if (!isConnected) {
                    // ✅ 2. 네트워크가 끊겼을 때의 방어 로직 실행
                    handleNetworkDisconnected()
                }
            }
        }
    }

    private fun handleNetworkDisconnected() {
        // 모달이 열려 있거나, 광고 로딩 중일 수 있으므로 두 상태 모두 안전하게 초기화합니다.
        _uiState.update { currentState ->
            currentState.copy(
                isShowAdModalDialog = false, // 모달 닫기
                isAdLoading = false          // 로딩 상태 해제
            )
        }

        // (선택) 광고 매니저의 상태도 초기화하여 꼬이지 않게 방지
        adManager.clearAd()
    }

    private fun observeAdStatus() {
        viewModelScope.launch {
            // 광고 상태를 관찰하여 null이 되면 자동으로 로드
            adManager.rewardedAd.collect { ad ->
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

                else -> {
                    moveToError(adRewardResponse)
                }
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

                else -> {
                    moveToError(response)
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

    fun showRewardedAdWithLoading(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onRewardFailed: () -> Unit,
    ) {
        // 🔒 방어막 추가: 이미 로딩 중이면 이후 로직을 아예 타지 않음 (연속 터치 완벽 방어)
        if (_uiState.value.isAdLoading) return

        val currentAd = adManager.rewardedAd.value

        if (currentAd != null) {
            // 1. 이미 광고가 있는 경우 즉시 노출
            _uiState.update { it.copy(isAdLoading = true) }
            showAd(currentAd, activity, onRewardEarned, onRewardFailed)
        } else {
            // 2. 광고가 없는 경우 로딩 시작 및 로드 대기
            _uiState.update { it.copy(isAdLoading = true) }
            adManager.loadAd() // 광고 로드 요청

            viewModelScope.launch {
                // 광고가 로드될 때까지(null이 아닐 때까지) 기다림
                try {
                    // 네트워크 불안정 환경 케이스 처리
                    // ✅ 5초(5000ms) 안에 광고가 null이 아닌 값으로 들어올 때까지 대기
                    val ad = withTimeout(5000L) {
                        adManager.rewardedAd.filterNotNull().first()
                    }

                    // 광고가 로드되면 로딩 상태 해제 후 노출
                    _uiState.update { it.copy(isAdLoading = false) }
                    showAd(ad, activity, onRewardEarned, onRewardFailed)

                } catch (e: TimeoutCancellationException) {
                    // ✅ 5초가 지나도 로드되지 않으면 타임아웃 예외 발생
                    // todo. 잠시 후 광고 시청을 시도해 주세요. Toast 메시지 띄우기
                    _uiState.update { it.copy(isAdLoading = false) }
                    Log.e("AdManager", "광고 로드 타임아웃: $e")
                }

            }
        }
    }

    private fun showAd(
        ad: RewardedAd,
        activity: Activity,
        onRewardEarned: () -> Unit,
        onRewardFailed: () -> Unit,
    ) {
        // 💡 1. 보상 획득 여부를 기록할 변수 (초기값: false)
        var isRewardEarned = false

        // 화면 전환 콜백 설정 (닫힘, 실패 등)
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                // 사용자가 광고 시청을 마치고(또는 중간에 건너뛰고) 'X(닫기)' 버튼이나 '뒤로 가기'를 눌러 원래 앱 화면으로 돌아온 순간
                adManager.clearAd()
                adManager.loadAd() // 다음을 위해 미리 로드

                // 💡 3. 광고가 닫혔을 때 검사
                if (!isRewardEarned) {
                    // 보상을 못 받고 닫혔다면 실패 콜백 실행
                    // 💡 2. 여기 추가! 노출에 실패하면 화면에 광고가 안 뜨므로 로딩 상태를 수동으로 해제해줘야 합니다.
                    _uiState.update { it.copy(isAdLoading = false) }
                    onRewardFailed()
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                // 광고 객체가 있어서 ad.show()를 불렀는데, 모종의 이유로 화면에 그리는 데 실패한 순간 호출
                // 1. 에러 로그 찍기
                Log.e("AdManager", "광고 노출 실패: ${error.message} (에러코드: ${error.code})")

                // 2. 고장난 현재 광고 버리기
                adManager.clearAd()
            }

            override fun onAdShowedFullScreenContent() {
                // 광고가 화면에 성공적으로 짠! 하고 나타난 바로 그 순간 호출
                _uiState.update { it.copy(isAdLoading = false) }
            }
        }

        // 2. 광고 노출 및 보상 콜백 설정 (이 부분이 전면 광고와 다름!)
        ad.show(activity) { rewardItem ->
            // ✅ 사용자가 광고를 끝까지 시청 완료 시 호출됨
            // 여기서 쿠폰을 증가시키는 서버 API(submitAdWatching)를 호출합니다.
            // 💡 2. 사용자가 광고를 끝까지 봤을 때 true로 변경!
            isRewardEarned = true
            onRewardEarned()
        }
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
                                    currentGoalCompleted = goal.isCompleted,
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
        return goal.isCompleted
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

        // 1️⃣ 목표 완료 시
        if (goal.isCompleted) {
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