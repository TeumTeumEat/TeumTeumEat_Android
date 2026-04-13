package com.teumteumeat.teumteumeat.ui.screen.c1_mypage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.mapper.toLable
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_response.GetGoalResponse
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.data.repository.notification.NotificationRepository
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.usecase.GetGoalListUseCase
import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.notification.GetPushNotificationStatusUseCase
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.NotificationSettingGuideType
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenStore
import com.teumteumeat.teumteumeat.utils.Utils.InfoUtil.getAppVersion
import com.teumteumeat.teumteumeat.utils.Utils.PrefsUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository,
    private val goalRepository: GoalRepository,
    private val getGoalListUseCase: GetGoalListUseCase,
    private val socialLoginRepository: SocialLoginRepository,
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val notificationRepository: NotificationRepository,
    val sessionManager: SessionManager,

    // + UseCase 주입 추가
    private val getPushNotificationStatusUseCase: GetPushNotificationStatusUseCase,

) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateMyPage())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(
        replay = 0,              // 재전달 ❌
        extraBufferCapacity = 1  // 순간 이벤트 유실 방지
    )
    val uiEvent = _uiEvent.asSharedFlow()

    val version = getAppVersion(application)

    private val appContext = application.applicationContext

    init {
        loadMyPageData()
    }

    // 1. 초기화 로직 최적화 (병렬 실행)
    fun loadMyPageData() {
        // 전체 로딩 상태 시작
        _uiState.update { it.copy(isLoading = true, appVersion = version) }
        _screenState.value = UiScreenState.Loading

        // 각 데이터를 병렬로 요청
        viewModelScope.launch {
            try {
                // async를 사용하여 3개의 작업을 동시에 시작
                val goalDeferred = async { loadUserGoal() }
                val accountDeferred = async { loadAccountInfo() } // launch 제거 버전
                val pushDeferred = async { fetchPushNotifiState() } // launch 제거 버전

                // 세 작업이 모두 끝날 때까지 병렬로 대기
                awaitAll(goalDeferred, accountDeferred, pushDeferred)

                _screenState.value = UiScreenState.Success
            } catch (e: Exception) {
                // 에러 처리
                _screenState.value = UiScreenState.Error(
                    message = e.message ?: "데이터를 불러오는 중 문제가 발생했습니다."
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getErrorState(
        message: String,
        onRetry: () -> Unit
    ): ErrorState {
        return ErrorState(
            title = "문제가 발생했어요",
            description = message,
            retryLabel = "다시 시도",
            onRetry = onRetry
        )
    }

    /**
     * 알림 상태를 서버와 기기 권한을 통합하여 가져옵니다.
     */
    fun fetchPushNotifiState() {
        viewModelScope.launch {
            // 1. UseCase 호출: 내부에서 서버 데이터와 기기 권한을 이미 연산해서 반환함
            when (val result = getPushNotificationStatusUseCase()) {
                is ApiResultV2.Success -> {
                    // 성공 시 통합된 Boolean 값(result.data)으로 UI 업데이트
                    _uiState.update {
                        it.copy(
                            isAlarmEnabled = result.data
                        )
                    }
                }

                else -> {
                    // 2. 에러 발생 시 공통 에러 처리 함수 호출
                    // 이 result.message 안에는 이미 map 과정에서 가공된 uiMessage가 들어있습니다.
                    moveToError(result)
                }
            }
        }
    }


    private suspend fun registerDeviceTokenInternal(): ApiResultV2<Unit> {

        val fcmToken = FcmTokenStore.get(appContext)
            ?: return ApiResultV2.UnknownError("디바이스 토큰이 없습니다.")

        val deviceType = "ANDROID"

        return notificationRepository.registerDeviceToken(
            token = fcmToken,
            deviceType = deviceType
        )
    }

    private suspend fun deleteDeviceTokenInternal(): ApiResultV2<Unit> {

        val fcmToken = FcmTokenStore.get(appContext)
            ?: return ApiResultV2.UnknownError("디바이스 토큰이 없습니다.")

        val deviceType = "ANDROID"

        return notificationRepository.deleteDeviceToken(
            token = fcmToken,
            deviceType = deviceType
        )
    }

    fun toogleAlarm(isAlarmEnabled: Boolean) {
        if (isAlarmEnabled) {
            // 알림을 켜는 경우
            if (Utils.UxUtils.isNotificationPermissionRequired()) {
                val isGranted = Utils.UiUtils.isPostNotificationsGranted(appContext)
                val hasDeniedBefore = PrefsUtil.hasNotificationDeniedOnce(appContext)

                if (isGranted) {
                    // 이미 권한이 있으면 서버 업데이트
                    updateAlarmInternal(true)
                } else if (hasDeniedBefore) {
                    // 이전에 거절한 적이 있으면 설정 유도 모달
                    _uiState.update { it.copy(notificationGuideType = NotificationSettingGuideType.ENABLE) }
                } else {
                    // 최초 요청이면 시스템 팝업 요청 트리거
                    _uiState.update { it.copy(requestNotificationPermission = true) }
                }
            } else {
                // Android 13 미만은 바로 서버 업데이트
                updateAlarmInternal(true)
            }
        } else {
            // 알림을 끄는 경우
            updateAlarmInternal(false)
        }
    }

    private fun updateAlarmInternal(isAlarmEnabled: Boolean) {
        viewModelScope.launch {
            val result = if (isAlarmEnabled) registerDeviceTokenInternal()
            else deleteDeviceTokenInternal()

            when (result) {
                is ApiResultV2.Success -> {
                    _uiState.update { it.copy(isAlarmEnabled = isAlarmEnabled) }
                }

                is ApiResultV2.SessionExpired -> {
                    userRepository.updateUserPushEnableSettings(isAlarmEnabled)
                    sessionManager.expireSession()
                }

                else -> moveToError(result)
            }
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        if (granted) {
            updateAlarmInternal(true)
        } else {
            // 거절 시 이력 저장
            PrefsUtil.saveNotificationDeniedOnce(appContext)
        }
        _uiState.update { it.copy(requestNotificationPermission = false) }
    }

    fun closeNotificationGuide() {
        _uiState.update { it.copy(notificationGuideType = NotificationSettingGuideType.NONE) }
    }

    fun onOpenedSettings() {
        _uiState.update { it.copy(isWaitingForPermissionUpdate = true) }
    }

    fun checkPermissionAfterReturn() {
        if (_uiState.value.isWaitingForPermissionUpdate) {
            val isGranted = Utils.UiUtils.isPostNotificationsGranted(appContext)
            if (isGranted) {
                updateAlarmInternal(true)
            }
            _uiState.update { it.copy(isWaitingForPermissionUpdate = false) }
        }
    }

    fun consumeNotificationPermissionRequest() {
        _uiState.update { it.copy(requestNotificationPermission = false) }
    }

    fun logout(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if(tokenLocalDataSource.getRefreshToken() == null){
                onSuccess()
            }
            when (val result = socialLoginRepository.logout(tokenLocalDataSource.getRefreshToken()!!)) {

                is ApiResultV2.Success -> {
                    onSuccess()
                }

                is ApiResultV2.SessionExpired -> {
                    // 이미 만료 → 그냥 로그아웃 처리
                    onSuccess()
                }

                else -> {
                    onError(result.uiMessage)
                }
            }
        }
    }

    fun withdrawUser() {
        viewModelScope.launch {
            when (val result = socialLoginRepository.withdrawUser()) {
                is ApiResultV2.Success -> {
                    // ✅ 1. 로컬 토큰 삭제
                    tokenLocalDataSource.clear()

                    // ✅ 2. 로그인 화면으로 이동
                    sessionManager.expireSession()
                }

                else -> {
                    moveToError(result)
                }
            }
        }
    }

    private suspend fun loadAccountInfo() {
        viewModelScope.launch {

            // 1️⃣ 로딩 시작
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            when (val result = userRepository.getAccountInfo()) {

                is ApiResultV2.Success -> {
                    val data = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginProvider = data.socialProvider.toUiText(),
                            socialProvider = data.socialProvider,
                            email = data.email
                        )
                    }
                }

                else -> {
                    moveToError(result)
                }
            }
        }
    }

    /**
     * 공통 에러 처리 함수
     * 세션 만료는 세션 매니저로 전달하고, 나머지는 로딩 해제 및 에러 메시지를 노출합니다.
     */
    suspend fun moveToError(result: ApiResultV2<*>) {
        when (result) {
            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
            }

            else -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        // UseCase의 map 함수에서 이미 uiMessage 로직이 적용된 message가 넘어옵니다.
                        // result.uiMessage 확장 프로퍼티를 사용하면 안전하게 가공된 메시지를 가져옵니다.
                        errorMessage = result.uiMessage
                    )
                }
            }
        }
    }

    private suspend fun loadUserGoal() {
        when (val result = goalRepository.getUserGoal()) {

            is ApiResultV2.Success -> {
                val userGoal = result.data

                when(userGoal.type){
                    DomainGoalType.CATEGORY -> {
                        _uiState.update { state ->
                            state.copy(
                                selectedTopic = userGoal.category?.path!!,
                                topicDescription = userGoal.prompt ?: "",
                                goalWeek = userGoal.studyPeriod,
                                goalDifficulty = userGoal.difficulty.toLable()
                            )
                        }
                    }
                    DomainGoalType.DOCUMENT -> {
                        _uiState.update { state ->
                            state.copy(
                                selectedTopic = userGoal.fileName!!,
                                topicDescription = userGoal.prompt ?: "",
                                goalWeek = userGoal.studyPeriod,
                                goalDifficulty = userGoal.difficulty.toLable()
                            )
                        }
                    }

                }
                _uiState.update { state ->
                    state.copy(
                        isSelGoalCompleted = userGoal.isCompleted
                    )
                }

            }

            else -> {
                moveToError(result)
            }
        }
    }

    private fun applyFirstGoal(goal: GetGoalResponse) {

        val (topic, description) =
            when (goal.type) {

                GoalTypeUiState.CATEGORY -> {
                    val category = goal.category
                    (category?.path ?: "미설정") to
                        (goal.prompt ?: "")
                }

                GoalTypeUiState.DOCUMENT -> {
                    (goal.fileName ?: "문서") to (goal.prompt ?: "입력한 프롬프트가 없습니다.")
                }

                GoalTypeUiState.NONE -> {
                    "잘못된 목표" to ""
                }
            }

        val difficultyText = when (goal.difficulty) {
            Difficulty.EASY -> "난이도 하"
            Difficulty.MEDIUM -> "난이도 중"
            Difficulty.HARD -> "난이도 상"
            Difficulty.NONE -> ""
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                // ⭐ 요구사항: 첫 번째 목표 표시
                selectedTopic = topic,
                topicDescription = description,
                goalWeek = goal.studyPeriod,
                goalDifficulty = difficultyText
            )
        }
    }
}

private fun SocialProvider.toUiText(): String =
    when (this) {
        SocialProvider.KAKAO -> "카카오 로그인"
        SocialProvider.GOOGLE -> "구글 로그인"
        SocialProvider.NONE -> "알 수 없는 로그인"
    }

sealed class UiEvent {
    // 화면 이동
    data object NavigateToLogin : UiEvent()
}
