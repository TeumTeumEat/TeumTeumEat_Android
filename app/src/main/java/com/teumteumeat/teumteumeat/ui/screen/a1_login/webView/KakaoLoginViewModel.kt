package com.teumteumeat.teumteumeat.ui.screen.a1_login.webView

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.AuthToken
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KakaoLoginViewModel @Inject constructor(
    private val tokenLocalDataSource: TokenLocalDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow<KakaoLoginUiState>(KakaoLoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * WebView에서 Authorization Code 수신
     */
    fun onKakaoAuthCodeReceived(
        accessToken: String,
        refreshToken: String
    ) {
        viewModelScope.launch {
            _uiState.value = KakaoLoginUiState.Loading
            Log.d("웹뷰 로그인 절차: ", "뷰모델 로직 시작")

            viewModelScope.launch {
                // 1️⃣ 토큰 객체 생성
                val authToken = AuthToken(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )

                // 2️⃣ Local 저장소에 저장
                tokenLocalDataSource.save(authToken)

                // 3️⃣ 로그인 성공 상태로 전환
                _uiState.value = KakaoLoginUiState.Success
            }
        }
    }

    /**
     * OAuth 실패 (취소, 에러 등)
     */
    fun onKakaoLoginFailed(message: String) {
        _uiState.value = KakaoLoginUiState.Error(message)
    }
}
