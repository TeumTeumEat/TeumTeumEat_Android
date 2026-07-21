package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.localdata.preference.HomePreference
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.domain.usecase.GetGoalListUseCase
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState.Error
import com.teumteumeat.teumteumeat.utils.date_change_reciver.DateChangeReceiver
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import com.teumteumeat.teumteumeat.utils.manager.ad.RewardedAdManager
import com.teumteumeat.teumteumeat.utils.monitor.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val getGoalListUseCase: GetGoalListUseCase,
    private val quizRepository: QuizRepository,
    val sessionManager: SessionManager,
    private val dateChangeReceiver: DateChangeReceiver,
    @ApplicationContext private val context: Context, // Context 주입 필요
    private val adManager: RewardedAdManager,
    private val networkConnection: NetworkConnection,
    private val savedStateHandle: SavedStateHandle, // 프로세스 죽음 대비
    private val analyticsLogger: TeumAnalyticsLogger,
    private val homePreference: HomePreference,
) : ViewModel() {

    // SavedStateHandle에 날짜를 저장 (메모리 유실 방지)
    private var lastDate: String?
        get() = savedStateHandle["last_checked_date"]
        set(value) {
            savedStateHandle["last_checked_date"] = value
        }

    private val _uiState = MutableStateFlow(UiStateHome())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    // 서버에서 받은 goal 캐싱 (SnackState 계산용)
    private var cachedGoal: UserGoal? = null

    // 중복 로드 방지 — 새 요청이 들어오면 진행 중인 이전 로드를 취소한다
    private var loadJob: Job? = null


    // home_view 이벤트 발화 날짜 기록: 같은 날 loadHomeState() 재호출(목표 변경 signal 등) 시
    // 중복 발화를 막고, 자정을 넘겨 사용하는 경우에는 새 날짜로 재발화한다 (DAU 측정 기준)
    private var lastLoggedHomeViewDate: String? = null

    init {
        // 강제 종료 후 복귀 시에도 저장된 음식 즉시 복원 (API 응답 전 기본값 노출 방지)
        homePreference.getSelectedFoodRes()?.let { savedFood ->
            _uiState.update { it.copy(selectedFoodRes = savedFood) }
        }

        // 실제 앱 구동 시에만 리시버 등록
        setupDateChangeReceiver()

        // ✅ 1. 네트워크 상태 감지 시작
        observeNetworkState()

        // 앱 시작 후 메인 액티비티 진입 시 광고 로드
        observeAdStatus()

        loadHomeState()
        // init에서 loadHomeState()를 실행했으므로 오늘 날짜를 기록해 둡니다.
        // ON_RESUME이 이어서 발생해도 같은 날이면 checkDateChangeOnResume()이 건너뜁니다.
        lastDate = LocalDate.now().toString()

        // 2. 목표 변경 / 퀴즈 완료 리프래시 시그널 감지
        viewModelScope.launch {
            goalRepository.refreshSignal.collect {
                loadHomeState(showLoading = false)
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

    /**
     * 홈화면 진입 이벤트(home_view)를 날짜별 1회 발화합니다.
     *
     * 퀴즈 상태가 서버 응답 후에만 정확하므로 init이 아닌 퀴즈 상태 조회 완료 시점에 호출합니다.
     *
     * @param quizDoneToday    당일 퀴즈 완료 여부 — "true" | "false" | 조회 실패 시 "unknown"
     * @param summaryDoneToday 당일 요약 생성 여부 — "true" | "false" | 조회 실패 시 "unknown"
     */
    private fun logHomeViewIfNeeded(quizDoneToday: String, summaryDoneToday: String) {
        val today = LocalDate.now().toString()
        if (lastLoggedHomeViewDate == today) return
        lastLoggedHomeViewDate = today
        analyticsLogger.logHomeView(
            quizDoneToday = quizDoneToday,
            summaryDoneToday = summaryDoneToday,
            date = today,
        )
    }

    /**
     * '오늘의 간식' 오브젝트 탭 이벤트(snack_tap)를 발화합니다 — 홈→퀴즈 전환율 측정용.
     *
     * 퍼널 이벤트이므로 발화 횟수를 제한하지 않고 탭할 때마다 매번 호출합니다.
     * 어떤 snackState에서든 발화하며, 전환율 계산 시 snack_state == "available"만 분모로 사용합니다.
     */
    fun onSnackTapped() {
        val state = _uiState.value
        val snackStateName = when (state.snackState) {
            SnackState.Available -> "available"
            is SnackState.Consumed -> "consumed"
            SnackState.Completed -> "completed"
        }
        analyticsLogger.logSnackTap(
            quizDoneToday = state.hasSolvedToday.toString(),
            snackState = snackStateName,
        )
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
                            // isShowAdModalDialog = true,
                            cupponCount = quizStatus.availableQuizCount, // 서버에서 받아온 새 개수
                            dailyAdRewardCount = quizStatus.dailyAdRewardCount,
                            canIssueCoupon = quizStatus.canIssueCoupon
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
                    _uiState.update {
                        it.copy(
                            isAdLoading = false,
                            errorMessage = "광고를 끝까지 시청해야 쿠폰이 지급됩니다."
                        )
                    }
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                // 광고 객체가 있어서 ad.show()를 불렀는데, 모종의 이유로 화면에 그리는 데 실패한 순간 호출
                // 1. 에러 로그 찍기
                Log.e("AdManager", "광고 노출 실패: ${error.message} (에러코드: ${error.code})")

                // 2. 고장난 현재 광고 버리기
                adManager.clearAd()
                adManager.loadAd() // 실패했으므로 즉시 새 광고 로드 시도

                _uiState.update {
                    it.copy(
                        isAdLoading = false,
                        // ✅ 요구사항: "쿠폰 충전을 다시 시도해주세요!" 메시지 전달
                        errorMessage = "쿠폰 충전을 다시 시도해주세요!"
                    )
                }
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

    /**
     * 앱이 백그라운드로 전환될 때 날짜를 기록하는 리시버를 등록합니다.
     * 테스트 방법: 터미널 zshrc 쉘에 아래 명령어를 입력합니다.
     * - adb -d shell am broadcast -a com.teumteumeat.test.ACTION_DATE_CHANGED
     * - '-d' 옵션은 device 를 지칭하는 옵션이다.
     * - 따라서 1개의 실기기를 연결 후 '디버깅'모든에서 위 명령어를 입력하여 테스트한다.
     */
    internal fun setupDateChangeReceiver() {

        dateChangeReceiver.setOnDateChangedListener {
            // 날짜가 바뀌었으므로 lastDate를 갱신합니다.
            // 앱이 포그라운드로 돌아올 때 ON_RESUME이 발생해도 날짜가 같으면 중복 호출되지 않습니다.
            lastDate = LocalDate.now().toString()
            loadHomeState()
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

    /**
     * [쿠폰 사용] 버튼 클릭 시 호출됩니다.
     * Consumed 상태를 유지한 채 SummaryActivity로 이동합니다(forceStream=true).
     * SummaryActivity 내부에서 에러 시 GET으로 폴백합니다.
     */
    fun useCoupon(
        onSuccess: (SummaryQuery, Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val query = _uiState.value.summaryQuery
        onSuccess(query, true)
    }

    // 테스트에서 감시(Spy)하기 위해 open 또는 internal로 선언
    internal fun onDateChangedTriggered() {
        loadHomeState()
    }

    /**
     * ON_RESUME 시 호출됩니다.
     * 오늘 날짜로 lastDate를 갱신하는 역할만 담당합니다.
     * loadHomeState()는 ON_RESUME에서 별도로 항상 호출됩니다.
     */
    fun checkDateChangeOnResume() {
        lastDate = LocalDate.now().toString()
    }

    private fun setRandomFood() {
        val food = _uiState.value.foodList.random()
        homePreference.saveTodayFood(food)
        _uiState.update { it.copy(selectedFoodRes = food) }
    }

    /**
     * 홈 진입 시 서버 기준 상태 로딩.
     * @param showLoading true이면 Loading 상태를 노출한다. ON_RESUME에서는 false를 사용해 UX 플리커를 방지한다.
     */
    fun loadHomeState(showLoading: Boolean = true) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (showLoading) _screenState.value = UiScreenState.Loading

            // API 응답 전 클라이언트 날짜 가드:
            // 오늘이 아닌 날 기록된 Consumed 상태라면 즉시 Available 전환.
            // 네트워크 오류로 API 호출이 실패해도 날짜 전환을 보장한다.
            if (_uiState.value.snackState is SnackState.Consumed
                && !homePreference.isSnackConsumedToday()
            ) {
                Log.d("HomeViewModel", "날짜 변경 감지 — Consumed → Available (클라이언트 가드)")
                _uiState.update { it.copy(snackState = SnackState.Available) }
            }

            Log.d("요약글 조회 디버깅", "홈화면 상태 가져옴 - 목표 조회 완료")


            // 1️⃣ 목표 조회
            when (val goalResult = goalRepository.getUserGoal()) {

                is ApiResultV2.Success -> {
                    val goal = goalResult.data
                    cachedGoal = goal
                    Log.d("user's current goal", "${goal}")

                    if (goal.goalId == -1L) {
                        _uiState.update { it.copy(isShowNewGoalGuideDialog = true) }
                    } else {
                        _uiState.update { it.copy(isShowNewGoalGuideDialog = false) }
                    }

                    // 목표 완료 상태라면 홈 화면 진입(로드) 시마다 매번 노출한다.
                    // (다른 화면 이동 후 재진입한 경우에도 다시 보여야 하므로 세션 단위로 dedup하지 않는다)
                    _uiState.update { it.copy(isShowGoalCompletedDialog = goal.isCompleted) }

                    // 2️⃣ 오늘 퀴즈 상태 조회
                    when (val quizResult = quizRepository.getUserQuizStatus()) {

                        is ApiResultV2.Success -> {
                            val quizStatus = quizResult.data

                            // 홈화면 진입 이벤트 (DAU 측정 기준, 날짜별 1회)
                            logHomeViewIfNeeded(
                                quizDoneToday = quizStatus.hasSolvedToday.toString(),
                                summaryDoneToday = quizStatus.hasCreatedToday.toString(),
                            )

                            // 현재 날짜 가져오기 (예: "2023-10-27")
                            val today = LocalDate.now().toString()

                            // 목표완료 팝업의 [진행중인 틈틈잇 선택하기] 버튼 활성화 여부 판단용
                            val hasRunningGoal = if (goal.isCompleted) {
                                when (val listResult = getGoalListUseCase()) {
                                    is ApiResultV2.Success -> listResult.data.goalResponses.hasAnyRunningGoal()
                                    else -> false
                                }
                            } else false

                            // 서버 응답 기준으로 오늘의 Consumed 날짜 갱신
                            // 다음 날 클라이언트 날짜 가드가 동작할 수 있도록 오늘 날짜를 기록한다
                            if (quizStatus.hasSolvedToday) {
                                homePreference.markSnackConsumedToday()
                            }

                            _uiState.update {
                                it.copy(
                                    fireState = resolveFireState(goal),

                                    // 🔥 서버 기준 값 저장
                                    hasSolvedToday = quizStatus.hasSolvedToday,
                                    hasCreatedToday = quizStatus.hasCreatedToday,
                                    lastCheckedDate = today, // ✅ 오늘 날짜로 갱신
                                    isFirstTime = quizStatus.isFirstTime,
                                    dailyAdRewardCount = quizStatus.dailyAdRewardCount,
                                    canIssueCoupon = quizStatus.canIssueCoupon,
                                    cupponCount = quizStatus.availableQuizCount,

                                    snackState = resolveSnackState(
                                        goal = goal,
                                        hasSolvedToday = quizStatus.hasSolvedToday,
                                    ),
                                    currentGoalCompleted = goal.isCompleted,
                                    summaryQuery = buildSummaryQuery(goal),
                                    isShowNewGoalGuideDialog = quizStatus.isCompleted || goal.goalId == -1L,
                                    hasRunningGoal = hasRunningGoal,

                                    // 같은 목표 재시작 → 저장된 음식 복원 / 목표 변경은 아래에서 처리
                                    selectedFoodRes = homePreference.getSelectedFoodRes()
                                        ?: it.selectedFoodRes
                                )
                            }

                            // 날짜가 바뀐 경우에만 음식 랜덤 선택 (목표 전환은 음식에 영향 없음)
                            if (homePreference.isFoodOutdated()) {
                                setRandomFood()
                            }

                            _screenState.value = UiScreenState.Success
                        }


                        is ApiResultV2.SessionExpired -> {
                            sessionManager.expireSession()
                        }

                        is ApiResultV2.ServerError,
                        is ApiResultV2.NetworkError,
                        is ApiResultV2.UnknownError -> {
                            // 퀴즈 상태 조회 실패 시에도 DAU 누락 방지를 위해 "unknown"으로 발화
                            logHomeViewIfNeeded(
                                quizDoneToday = "unknown",
                                summaryDoneToday = "unknown",
                            )
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
        }
    }

    /* ================= 상태 계산 ================= */

    private fun resolveFireState(goal: UserGoal): FireState = FireState.Burning


    /**
     * 음식(Snack) 상태의 단일 결정 함수
     */
    private fun resolveSnackState(
        goal: UserGoal,
        hasSolvedToday: Boolean
    ): SnackState {

        if (goal.isCompleted) {
            return SnackState.Completed
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

    // 진행 가능한 목표 기준: isCompleted == false
    private fun List<GetGoalResponse>.hasAnyRunningGoal(): Boolean {
        return any { !it.isCompleted }
    }

    private fun buildSummaryQuery(goal: UserGoal): SummaryQuery =
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
        try {
            // context.unregisterReceiver(dateChangeReceiver)
        } catch (e: Exception) {
            // Log.e("HomeViewModel", "Receiver unregister error", e)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissGoalCompletedDialog() {
        _uiState.update { it.copy(isShowGoalCompletedDialog = false) }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    /**
     * 앱이 백그라운드로 전환될 때 현재 날짜를 기록합니다.
     */
    fun saveCurrentDate() {
        val today = java.time.LocalDate.now().toString()
        lastDate = today
        _uiState.update { it.copy(lastCheckedDate = today) }
        Log.d("HomeViewModel", "백그라운드 전환: 날짜 기록 ($today)")
    }

    fun addTestQuizCount() {
        viewModelScope.launch {
            when (val result = quizRepository.addTestQuizCount()) {
                is ApiResultV2.Success -> {
                    _uiState.update { it.copy(toastMessage = "퀴즈 풀이 횟수 +1 추가됨") }
                    updateUserQuizStatus()
                }
                else -> moveToError(result)
            }
        }
    }

    fun resetAdReward() {
        viewModelScope.launch {
            when (val result = quizRepository.resetAdReward()) {
                is ApiResultV2.Success -> {
                    _uiState.update { it.copy(toastMessage = "쿠폰 상태 초기화 완료") }
                    updateUserQuizStatus()
                }
                else -> moveToError(result)
            }
        }
    }

    fun resetGoal() {
        viewModelScope.launch {
            when (val result = quizRepository.resetGoal()) {
                is ApiResultV2.Success -> {
                    _uiState.update { it.copy(toastMessage = "목표 상태 초기화 완료") }
                    loadHomeState()
                }
                else -> moveToError(result)
            }
        }
    }

}