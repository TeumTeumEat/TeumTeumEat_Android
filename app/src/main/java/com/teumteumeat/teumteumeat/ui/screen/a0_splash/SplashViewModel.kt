package com.teumteumeat.teumteumeat.ui.screen.a0_splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.repository.login.AutoLogin
import com.teumteumeat.teumteumeat.domain.usecase.AutoLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val autoLoginUseCase: AutoLoginUseCase,
    private val tokenLocalDataSource: TokenLocalDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Idle)
    val uiState = _uiState.asStateFlow()

    init {
        // todo: testCode. 구글로그인 테스트 위해 리프레쉬 토큰 초기화용 함수
        clearAllToken()
        socialLogin()
    }

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

            when (result) {
                is AutoLogin.Success -> {
                    _uiState.value = SplashUiState.Success
                }

                is AutoLogin.Fail -> {
                    val errMsg = result.message
                    Log.d("자동 로그인 로직", "로그인 실패사유: ${errMsg}")
                    _uiState.value = SplashUiState.Error(errMsg)
                }
            }
        }
    }
}
