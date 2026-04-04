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
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.category.CategoryRepository
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.model.goal.UserGoal
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ProcessingUiState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState.*
import com.teumteumeat.teumteumeat.utils.date_change_reciver.DateChangeReceiver
import com.teumteumeat.teumteumeat.utils.manager.ad.RewardedAdManager
import com.teumteumeat.teumteumeat.utils.monitor.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val quizRepository: QuizRepository,
    private val documentRepository: DocumentRepository,
    private val categoryRepository: CategoryRepository,
    val sessionManager: SessionManager,
    private val dateChangeReceiver: DateChangeReceiver,
    @ApplicationContext private val context: Context, // Context 주입 필요
    private val adManager: RewardedAdManager,
    private val networkConnection: NetworkConnection,
    private val savedStateHandle: SavedStateHandle, // 프로세스 죽음 대비
) : ViewModel() {

    // SavedStateHandle에 날짜를 저장 (메모리 유실 방지)
    private var lastDate: String?
        get() = savedStateHandle["last_checked_date"]
        set(value) { savedStateHandle["last_checked_date"] = value }

    private val _uiState = MutableStateFlow(UiStateHome())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    // 서버에서 받은 goal 캐싱 (SnackState 계산용)
    private var cachedGoal: UserGoal? = null

    private var processingJob: Job? = null

    init {
        // 실제 앱 구동 시에만 리시버 등록
        // 만료된 목표일 때 만
        setupDateChangeReceiver()

        // ✅ 1. 네트워크 상태 감지 시작
        observeNetworkState()

        // 앱 시작 후 메인 액티비티 진입 시 광고 로드
        observeAdStatus()

        loadHomeState()
        // 2. 목표 변경 리프래시 시그널 감지
        viewModelScope.launch {
            // todo. 목표를 완료하고 돌아왔을 때도 loadHomeState() 호출 되는지 확인
            goalRepository.refreshSignal.collect {
                // 다른 액티비티에서 목표를 변경하고 돌아왔을 때 호출됨
                loadHomeState()
            }
        }
    }

    fun checkAndLoadHomeState() {
        val today = LocalDate.now().toString()
        val savedDate = lastDate

        Log.d("HomeViewModel", "체크 시작 - 저장된 날짜: $savedDate, 오늘: $today")

        // 💡 날짜가 다를 때만(null 포함) API를 호출합니다.
        if (savedDate != today) {
            loadHomeState()
            // 성공/실패 여부와 관계없이 일단 오늘 체크했음을 기록 (중복 호출 방지)
            lastDate = today
            _uiState.update { it.copy(lastCheckedDate = today) }
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

    internal fun setupDateChangeReceiver() {

        dateChangeReceiver.setOnDateChangedListener {
            // 💡 자정이 되면 서버에 현재 상태(쿠폰, 생성 여부 등)를 다시 물어봅니다.
            // 서버가 날짜 변경을 판단하여 hasCreatedToday = false를 줄 것입니다.
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

    /**
     * [쿠폰 사용] 버튼 클릭 시 호출되는 비즈니스 로직입니다.
     *
     * 동작 순서:
     * 1. 현재 사용자의 목표 타입(CATEGORY 또는 DOCUMENT)을 확인합니다.
     * 2. 해당 타입에 맞는 '오늘의 요약글 생성' API(POST)를 호출합니다.
     * 3. 성공 시:
     *    - 서버의 쿠폰 개수 및 퀴즈 상태를 최신화하기 위해 [updateUserQuizStatus]를 호출합니다.
     *    - 생성된 요약글의 ID를 포함하여 [SummaryQuery]를 업데이트한 후 [onSuccess] 콜백을 통해 화면 전환을 트리거합니다.
     * 4. 실패 시: [onError] 콜백을 통해 에러 메시지를 전달합니다.
     */
    fun useCoupon(
        onSuccess: (SummaryQuery) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // 1. 현재 홈 화면에 설정된 요약 정보를 가져옵니다.
            val currentState = _uiState.value
            val query = _uiState.value.summaryQuery

            // ✅ [추가] 이미 오늘 요약글을 생성했다면 (쿠폰을 이미 소모한 상태)
            // 로딩 애니메이션이나 API 호출 없이 바로 조회 화면으로 이동합니다.
            /*            if (currentState.hasCreatedToday) {
                            onSuccess(query)
                            return@launch
                            // 만약 hasCreatedToday는 true인데 documentId가 없다면 아래 생성 로직을 타게 하거나
                            // 별도의 조회를 먼저 수행하도록 안전장치를 둘 수 있습니다.
                        }*/


            // 로딩 상태 시작
            _screenState.value = UiScreenState.Loading
            _uiState.update { 
                it.copy(
                    loadingTitle = "새로운 퀴즈를 만들고 있어요!",
                    loadingMessage = "잠시만 기다려주세요."
                )
            }
            // 로딩 애니메이션 시작 (10초 동안 차올랐다가 다시 빠지는 무한 루프)
            processingJob?.cancel()
            processingJob = launch {
                var currentProgress = 0f
                var isIncreasing = true
                val interval = 50L           // 0.05초마다 부드럽게 업데이트
                val stepChange = 0.01f        // 업데이트 당 변화량 (약 5초에 100% 도달)

                while (isActive) {
                    if (isIncreasing) {
                        currentProgress += stepChange
                        if (currentProgress >= 1f) {
                            currentProgress = 1f
                            isIncreasing = false // 다 차면 감소 모드로 전환
                        }
                    } else {
                        currentProgress -= stepChange
                        if (currentProgress <= 0f) {
                            currentProgress = 0f
                            isIncreasing = true // 다 빠지면 다시 증가 모드로 전환
                        }
                    }

                    _uiState.update {
                        it.copy(processingState = ProcessingUiState(progress = currentProgress))
                    }
                    delay(interval)
                }
            }

            // 2. 목표 타입에 따라 카테고리 기반 또는 문서(PDF) 기반 요약글 생성 API를 분기 호출합니다.
            val result = when (query.goalType) {
                DomainGoalType.CATEGORY -> {
                    categoryRepository.createDailyCategoryDocument(query.categoryId ?: -1L)
                }

                DomainGoalType.DOCUMENT -> {
                    documentRepository.createDocumentSummary(
                        query.goalId.toInt(),
                        query.documentId?.toInt() ?: -1
                    )
                }
            }

            // API 응답 도착 시 애니메이션 중단 및 100% 처리
            processingJob?.cancel()
            _uiState.update { it.copy(processingState = ProcessingUiState(progress = 1f)) }

            when (result) {
                is ApiResultV2.Success -> {
                    // 3. 성공 시: 사용한 쿠폰 반영을 위해 유저의 퀴즈 상태(쿠폰 개수 등)를 서버로부터 다시 조회합니다.
                    updateUserQuizStatus()

                    // 4. API 응답으로 받은 새로운 문서 ID(documentId)를 Query 객체에 업데이트합니다.
                    //    이를 통해 SummaryActivity 진입 시 올바른 요약글을 조회할 수 있게 합니다.
                    val updatedQuery = when (val data = result.data) {
                        is com.teumteumeat.teumteumeat.data.network.model_response.DailyCategoryDocument -> {
                            query.copy(documentId = data.documentId)
                        }

                        is com.teumteumeat.teumteumeat.ui.screen.b1_summary.DocumentSummaryResponse -> {
                            query.copy(documentId = data.documentId.toLong())
                        }

                        else -> query
                    }

                    _screenState.value = UiScreenState.Success
                    _uiState.update { it.copy(processingState = null) }
                    // 5. 업데이트된 정보를 UI 레이어(HomeScreen)로 전달하여 화면 이동을 수행합니다.
                    onSuccess(updatedQuery)
                }

                is ApiResultV2.SessionExpired -> {
                    _screenState.value = UiScreenState.Idle
                    sessionManager.expireSession()
                }

                else -> {
                    _screenState.value = UiScreenState.Success
                    _uiState.update { it.copy(processingState = null) }
                    // API 호출 실패 시 에러 메시지 전달
                    onError(result.uiMessage)
                }
            }
        }
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
     * 홈 진입 시 서버 기준 상태 로딩 및 요약글 자동 생성 로직
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

                            // 현재 날짜 가져오기 (예: "2023-10-27")
                            val today = LocalDate.now().toString()
                            val currentState = _uiState.value

                            // 💡 [핵심 방어 로직]
                            // 1. 오늘 요약글이 생성되지 않았고 (Server Status)
                            // 2. 현재 진행 중인 목표가 완료되지 않았을 때만 자동 생성 실행
                            val shouldAutoGenerate = !quizStatus.hasCreatedToday &&
                                    !goal.isCompleted

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

                                    // 🔥 HomeViewModel에서만 SnackState 분기
                                    snackState = resolveSnackState(
                                        goal = goal,
                                        hasSolvedToday = quizStatus.hasSolvedToday,
                                    ),
                                    currentGoalCompleted = goal.isCompleted,
                                    summaryQuery = buildSummaryQuery(goal),
                                    isShowGoalExpiredDialog = quizStatus.isCompleted // ✅ 퀴즈 상태의 isCompleted 기반으로 모달 노출 여부 결정
                                )
                            }
                            // update 호출 직후 확인
                            Log.d(
                                "디버깅_업데이트",
                                "4. 업데이트 완료 후 실제 State 값: ${_uiState.value.summaryQuery}"
                            )

                            // 🔥 [핵심 로직] 자정이 지나 hasCreatedToday가 false가 되었다면 자동으로 요약글 생성
                            // 단, 이미 목표가 완료되었거나(isCompleted) 생성 중일 때는 제외
                            if (shouldAutoGenerate) {
                                // 자동 생성 시에도 로딩 화면에 진행 바를 표시하기 위해 loadingTitle 등을 설정
                                _uiState.update { 
                                    it.copy(
                                        loadingTitle = "새로운 퀴즈를 만들고 있어요!",
                                        loadingMessage = "새로운 하루가 시작되어 퀴즈를 준비하고 있어요."
                                    )
                                }
                                autoGenerateDailySummary(buildSummaryQuery(goal))
                            } else {                                // 자동 생성이 필요 없는 경우에만 즉시 Success로 전환
                                _screenState.value = UiScreenState.Success
                            }
                        }

                        is ApiResultV2.SessionExpired -> {
                            sessionManager.expireSession()
                        }

                        is ApiResultV2.ServerError,
                        is ApiResultV2.NetworkError,
                        is ApiResultV2.UnknownError -> {
                            //_screenState.value = Error(quizResult.uiMessage)
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


    /**
     * 자정이 지났을 때 백그라운드에서 요약글을 자동으로 생성합니다.
     */
    private fun autoGenerateDailySummary(query: SummaryQuery) {
        viewModelScope.launch {
            // 시작시 로딩 페이지를 표시한다.
            _uiState.update {
                it.copy(
                    toastMessage = "오늘도 틈틈잇! 오늘의 요약글을 생성 합니다!",
                    loadingMessage = "새로운 퀴즈를 만들고 있어요!"
                )
            }


            val result = when (query.goalType) {
                DomainGoalType.CATEGORY -> {
                    categoryRepository.createDailyCategoryDocument(query.categoryId ?: -1L)
                }

                DomainGoalType.DOCUMENT -> {
                    documentRepository.createDocumentSummary(
                        query.goalId.toInt(),
                        query.documentId?.toInt() ?: -1
                    )
                }
            }


            if (result is ApiResultV2.Success) {
                // 생성 성공 시 내부 상태 갱신 (쿠폰 차감 반영 등)
                _uiState.update {
                    it.copy(
                        hasCreatedToday = true,
                        toastMessage = "오늘의 요약글 생성이 완료되었습니다!",
                    )
                }
                updateUserQuizStatus()
                Log.d("HomeViewModel", "자정 경과: 요약글 자동 생성 완료")
                 _screenState.value = UiScreenState.Success
            } else {
                // 실패 시 다음 loadHomeState() 호출 때 재시도할 수 있도록
                // lastCheckedDate를 초기화하거나 로그를 남깁니다.
                _uiState.update { it.copy(lastCheckedDate = null) }
                Log.e("HomeViewModel", "요약글 자동 생성 실패: ${result.uiMessage}")
                 _screenState.value = UiScreenState.Success // 실패하더라도 홈화면은 보여줌
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
            context.unregisterReceiver(dateChangeReceiver)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Receiver unregister error", e)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
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



}