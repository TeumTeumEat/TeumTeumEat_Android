package com.teumteumeat.teumteumeat.ui.screen.a0_splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResult
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.login.AutoLogin
import com.teumteumeat.teumteumeat.domain.usecase.AutoLoginUseCase
import com.teumteumeat.teumteumeat.domain.usecase.on_boarding.GetOnboardingCompletedUseCase
import com.teumteumeat.teumteumeat.ui.screen.a0_splash.SplashUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val autoLoginUseCase: AutoLoginUseCase,
    private val getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        // todo: testCode. 구글로그인 테스트 위해 리프레쉬 토큰 초기화용 함수
        // clearAllToken()
        // socialLogin()
    }

    /**
     * 🔥 로티 애니메이션 종료 시 호출
     */
    fun onAnimationFinished() {
        socialLogin()
    }

    /**
     * 테스트용 (구글 로그인 테스트 시)
     */
    fun clearAllToken() {
        viewModelScope.launch {
            tokenLocalDataSource.clear()
        }
    }

    private fun socialLogin() {
        Log.d("소셜 로그인", "뷰모델 함수 호출")
        viewModelScope.launch {
            _uiState.value = SplashUiState.Loading

            val result = autoLoginUseCase()
            Log.d("자동 로그인 로직", "결과: ${result}")

            when (val loginResult = autoLoginUseCase()) {
                is AutoLogin.Success -> handleAutoLoginSuccess()
                is AutoLogin.Fail -> handleLoginFail(loginResult)
            }
        }
    }

    /**
     * 자동 로그인 성공 → 온보딩 여부 확인
     */
    private suspend fun handleAutoLoginSuccess() {
        when (val result = getOnboardingCompletedUseCase()) {

            is ApiResult.Success -> {
                val completed = result.data.completed
                Log.d("온보딩 로직: ", "온보딩 완료 여부")

                _uiState.value = SplashUiState.Success(
                    nextRoute = if (completed) {
                        SplashRoute.MAIN
                    } else {
                        SplashRoute.ON_BOARDING
                    }
                )
            }

            is ApiResult.SessionExpired -> {
                _uiState.value = SplashUiState.Error(
                    message = result.message,
                    nextRoute = SplashRoute.LOGIN
                )
            }

            is ApiResult.NetworkError -> {
                _uiState.value = SplashUiState.Error(
                    message = result.message,
                    nextRoute = SplashRoute.LOGIN
                )
            }

            is ApiResult.ServerError -> {
                _uiState.value = SplashUiState.Error(
                    message = result.message,
                    nextRoute = SplashRoute.LOGIN
                )
            }

            is ApiResult.UnknownError -> {
                _uiState.value = SplashUiState.Error(
                    message = result.message,
                    nextRoute = SplashRoute.LOGIN
                )
            }
        }
    }

    private fun handleLoginFail(result: AutoLogin.Fail) {
        _uiState.value = SplashUiState.Error(
            message = result.message,
            nextRoute = SplashRoute.LOGIN
        )
    }




}
