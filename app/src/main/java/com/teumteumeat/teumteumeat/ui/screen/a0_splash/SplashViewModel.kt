package com.teumteumeat.teumteumeat.ui.screen.a0_splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.login.AutoLogin
import com.teumteumeat.teumteumeat.domain.model.on_boarding.OnboardingDecision
import com.teumteumeat.teumteumeat.domain.usecase.AutoLoginUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.GetOnboardingCompletedUseCase
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
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SplashUiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        // todo: testCode. 구글로그인 테스트 위해 리프레쉬 토큰 초기화용 함수
        // clearAllToken()
        // socialLogin()
    }

    /**
     * 🔥 로티 애니메이션 종료 시 호출
     */
    fun onAnimationFinished() {
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

    private fun tryAutoLogin() {
        Log.d("소셜 로그인", "뷰모델 함수 호출")
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
                    Log.d("소셜 로그인", "세션 만료, 리프레쉬 토큰으로 재발급 필요")
                    showNetworkError(
                        message = result.message,
                        retry = { viewModelScope.launch { tryAutoLogin() } }
                    )
                    setLoginRequiredState(result.message)
                }

                is AutoLogin.Fail -> {
                    showNetworkError(
                        message = result.message,
                        retry = { viewModelScope.launch { tryAutoLogin() } }
                    )
                    setLoginRequiredState(result.message)
                }

                is AutoLogin.NetWorkError -> {
                    showNetworkError(
                        message = result.message,
                        retry = { viewModelScope.launch { tryAutoLogin() } }
                    )
                    setNetworkRequiredState(result.message)
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun checkOnboardingCompleted() {
        when (val result = getOnboardingCompletedUseCase()) {

            is OnboardingDecision.GoMain -> {
                _uiEvent.emit(SplashUiEvent.NavigateToMain)
            }

            is OnboardingDecision.GoOnboarding -> {
                _uiEvent.emit(SplashUiEvent.NavigateToOnboarding)
            }

            is OnboardingDecision.NeedLogin -> {
                showNetworkError(
                    message = result.message,
                    retry =  { viewModelScope.launch { checkOnboardingCompleted() } }
                )
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
                    retryLabel = "다시 시도하기",
                    onRetry = {
                        viewModelScope.launch {
                            tryAutoLogin()
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
