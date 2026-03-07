package com.teumteumeat.teumteumeat.ui.screen.a0_splash

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.login.AutoLogin
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingDecision
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.auth.AutoLoginUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.GetOnboardingCompletedUseCase
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.utils.Utils.PrefsUtil
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val autoLoginUseCase: AutoLoginUseCase,
    private val getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase,
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val remoteConfig: FirebaseRemoteConfig,
    val sessionManager: SessionManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SplashUiEvent>(
        replay = 0,
        extraBufferCapacity = 0
    )
    val uiEvent = _uiEvent.asSharedFlow()


    private fun checkAppVersion() {

        val minVersion =
            remoteConfig.getLong("android_min_version_code")

        val latestVersion =
            remoteConfig.getLong("android_latest_version_code")

        val forceEnabled =
            remoteConfig.getBoolean("force_update_enabled")

        val forceMessage =
            remoteConfig.getString("force_update_message")

        val optionalMessage =
            remoteConfig.getString("optional_update_message")

        val currentVersionCode = BuildConfig.VERSION_CODE.toLong()

        viewModelScope.launch {
            when {
                forceEnabled && currentVersionCode < minVersion -> {
                    _uiEvent.emit(
                        SplashUiEvent.ShowForceUpdate(forceMessage)
                    )
                }

                currentVersionCode < latestVersion -> {
                    _uiEvent.emit(
                        SplashUiEvent.ShowOptionalUpdate(optionalMessage)
                    )
                }

                // 👉 여기서는 기본 동작으로 이후 동작 진행
                else -> {}

            }
        }
    }


    /**
     * 🔥 로티 애니메이션 종료 시 호출
     */
    fun onAnimationFinished() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 서버값을 확실히 가져온 후 버전 체크
                    checkAppVersion()
                } else {
                    // 실패 시 기본 동작 진행
                    tryAutoLogin()
                }
            }
    }

    /**
     * 토큰 및 로그인 정보 삭제: 인증에러가 발생해서 로그인 화면으로 이동시, 기기에 저장된 토큰 삭제
     */
    fun clearAllToken() {
        viewModelScope.launch {
            tokenLocalDataSource.clear()
        }
    }

    fun tryAutoLogin() {
        Log.d("${this@SplashViewModel}", "try Auto Login")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorState = null
                )
            }

            when (val result = autoLoginUseCase()) {
                is AutoLogin.Success -> {
                    Log.d("SplshVM", "리프래쉬 토큰 확인: ${tokenLocalDataSource.getRefreshToken()}")
                    checkOnboardingCompleted()
                }
                is AutoLogin.SessionExpired -> {
                    sessionManager.expireSession()
                }
                else -> {
                    // [수정 포인트] 네트워크 오류 등으로 실패한 경우
                    // 토큰이 이미 기기에 있다면(리프레시 토큰 존재) 메인으로 이동시킵니다.
                    if (tokenLocalDataSource.getRefreshToken() != null) {
                        checkOnboardingCompleted() // 또는 직접 NavigateToMain 호출
                    } else {
                        sessionManager.expireSession()
                    }
                }
            }

            _uiState.update { it.copy(isLoading = false, errorState = null) }
        }
    }

    private suspend fun checkOnboardingCompleted() {

        // 1. 로컬에 저장된 온보딩 완료 여부를 먼저 확인 (오프라인 플래그 참조)
        val isCompleted = PrefsUtil.isOnboardingCompleted(getApplication(context))

        when (val result = getOnboardingCompletedUseCase()) {
            is OnboardingDecision.GoMain -> {
                Log.d("SplashVM", "Emit NavigateToMain")
                PrefsUtil.setOnboardingCompleted(getApplication(context), true)
            }

            is OnboardingDecision.GoOnboarding -> {
                Log.d("SplashVM", "Emit NavigateToOnboarding")
                // _uiEvent.emit(SplashUiEvent.NavigateToOnboarding)
            }

            is OnboardingDecision.NeedLogin -> {
                // 🔑 핵심: 에러 UI ❌
                Log.d("SplashVM", "Emit NavigateToLogin")
                // _uiEvent.emit(SplashUiEvent.NavigateToLogin)
            }
        }

        if (isCompleted) {
            // 온보딩을 이미 했다면 메인으로 이동
            Log.d("SplashVM", "Emit NavigateToMain (Offline Flag Checked)")
            _uiEvent.emit(SplashUiEvent.NavigateToMain)
        } else {
            // 온보딩 기록이 없다면 온보딩 화면으로 이동
            // todo. 네트워크가 연결되어 있지 않다면 로그인 화면으로 fall back
            Log.d("SplashVM", "Emit NavigateToOnboarding")
            _uiEvent.emit(SplashUiEvent.NavigateToOnboarding)
        }
    }

    private fun showNetworkError(
        message: String,
        retry: () -> Unit
    ) {
        _uiState.update {
            it.copy(
                errorState = ErrorState(
                    title = "다시 한번 접속해주세요!",
                    description = message,
                    onRetry = retry
                )
            )
        }
    }

    private fun setGoLoginState() {
        _uiState.update {
            it.copy(
                isLoading = false,
            )
        }
        viewModelScope.launch {
            clearAllToken()
            _uiEvent.emit(SplashUiEvent.NavigateToLogin)
        }
    }

    private fun setLoginRequiredState(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorState = ErrorState(
                    title = "인증 에러가 발생하였습니다!",
                    description = message,
                    retryLabel = "로그인 화면으로 이동",
                    onRetry = {
                        viewModelScope.launch {
                            clearAllToken()
                            _uiEvent.emit(SplashUiEvent.NavigateToLogin)
                        }
                    }
                )
            )
        }
    }


    private fun setNetworkRequiredState(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorState = ErrorState(
                    title = "다시한번 접속해주세요",
                    description = message,
                    retryLabel = "다시 시도",
                    onRetry = {
                        viewModelScope.launch {
                            tryAutoLogin()
                        }
                    },
                    secondaryLabel = "재 로그인",
                    onSecondaryAction = {
                        viewModelScope.launch {
                            clearAllToken()
                            _uiEvent.emit(SplashUiEvent.NavigateToLogin)
                        }
                    }
                )
            )
        }
    }

    fun dismissError() {
        _uiState.update {
            it.copy(
                errorState = null
            )
        }
    }

}
