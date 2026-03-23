package com.teumteumeat.teumteumeat.ui.screen.c1_mypage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.teumteumeat.teumteumeat.domain.model.goal.mapDifficultyToKorean
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils.FcmTokenStore
import com.teumteumeat.teumteumeat.utils.Utils.InfoUtil.getAppVersion
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun loadMyPageData() {
        viewModelScope.launch {
            loadUserGoal()
            loadAccountInfo()
            fetchPushNotifiState()
            _uiState.update {
                it.copy(
                    appVersion = version
                )
            }
            _screenState.value = UiScreenState.Success
        }
    }

    suspend fun fetchPushNotifiState(){
        viewModelScope.launch {
            // 로딩 시작 로직 (필요시 추가)

            when (val result = userRepository.getUserPushEnableSettings()) {
                is ApiResultV2.Success -> {
                    // 성공 시 데이터 업데이트
                    _uiState.update {
                        it.copy(
                            isAlarmEnabled = result.data.pushEnabled
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    // 세션 만료 처리 (예: 로그인 화면 이동 이벤트 발생)
                    sessionManager.expireSession()
                }

                else -> {
                    // ServerError, NetworkError, UnknownError 통합 처리
                    // 확장 프로퍼티인 uiMessage를 활용해 가독성 높게 처리 가능합니다.
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
        viewModelScope.launch {
            if (isAlarmEnabled){
                when(val result = registerDeviceTokenInternal()) {
                    is ApiResultV2.Success -> {
                        _uiState.update {
                            it.copy(
                                isAlarmEnabled = true
                            )
                        }
                    }
                    is ApiResultV2.SessionExpired -> {
                        userRepository.updateUserPushEnableSettings(isAlarmEnabled)
                        sessionManager.expireSession()
                    }
                    else -> {
                        moveToError(result)
                    }
                }
            }else{
                when(val result = deleteDeviceTokenInternal()) {
                    is ApiResultV2.Success -> {
                        _uiState.update {
                            it.copy(
                                isAlarmEnabled = false
                            )
                        }
                    }
                    is ApiResultV2.SessionExpired -> {
                        userRepository.updateUserPushEnableSettings(isAlarmEnabled)
                        sessionManager.expireSession()
                    }
                    else -> {
                        moveToError(result)
                    }
                }
            }
        }
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

    private suspend fun moveToError(result: ApiResultV2<*>) {
        when (result) {
            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
            }

            is ApiResultV2.NetworkError -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.uiMessage
                    )
                }
            }

            is ApiResultV2.ServerError -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.uiMessage
                    )
                }
            }
            else -> {


                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "알 수 없는 오류가 발생했습니다."
                    )
                }
            }
        }

    }

    private fun loadMyPage() {
        viewModelScope.launch {

            // 1️⃣ 로딩 시작
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getGoalListUseCase()) {

                is ApiResultV2.Success -> {
                    val firstGoal = result.data.goalResponses.firstOrNull()

                    if (firstGoal == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "설정된 목표가 없습니다."
                            )
                        }
                        return@launch
                    }

                    // 2️⃣ Goal → UiState 매핑
                    applyFirstGoal(firstGoal)
                }

                else -> {
                    moveToError(result)
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
                                goalDifficulty = mapDifficultyToKorean(userGoal.difficulty)
                            )
                        }
                    }
                    DomainGoalType.DOCUMENT -> {
                        _uiState.update { state ->
                            state.copy(
                                selectedTopic = userGoal.fileName!!,
                                topicDescription = userGoal.prompt ?: "",
                                goalWeek = userGoal.studyPeriod,
                                goalDifficulty = mapDifficultyToKorean(userGoal.difficulty)
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
