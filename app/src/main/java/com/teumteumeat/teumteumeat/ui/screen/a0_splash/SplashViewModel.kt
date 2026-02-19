package com.teumteumeat.teumteumeat.ui.screen.a0_splash

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
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SplashUiEvent>(
        replay = 0,
        extraBufferCapacity = 0
    )
    val uiEvent = _uiEvent.asSharedFlow()

    init {

    }

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

                // 👉 여기서는 기본 동작으로 이동
                else -> {}

            }
        }
    }


    /**
     * 🔥 로티 애니메이션 종료 시 호출
     */
    fun onAnimationFinished() {
        remoteConfig
            .fetchAndActivate()
            .addOnCompleteListener {
                checkAppVersion()
            }
        tryAutoLogin()
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
                    checkOnboardingCompleted()
                }
                is AutoLogin.SessionExpired -> {
                    sessionManager.expireSession()
                }
                else -> {
                    sessionManager.expireSession()
                }
            }

            _uiState.update { it.copy(isLoading = false, errorState = null) }
        }
    }

    private suspend fun checkOnboardingCompleted() {
        when (val result = getOnboardingCompletedUseCase()) {

            is OnboardingDecision.GoMain -> {
                Log.d("SplashVM", "Emit NavigateToMain")
                _uiEvent.emit(SplashUiEvent.NavigateToMain)
            }

            is OnboardingDecision.GoOnboarding -> {
                Log.d("SplashVM", "Emit NavigateToOnboarding")

                _uiEvent.emit(SplashUiEvent.NavigateToOnboarding)
            }

            is OnboardingDecision.NeedLogin -> {
                // 🔑 핵심: 에러 UI ❌
                Log.d("SplashVM", "Emit NavigateToLogin")
                _uiEvent.emit(SplashUiEvent.NavigateToLogin)
            }
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
