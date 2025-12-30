package com.teumteumeat.teumteumeat.ui.screen.a1_login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_request.AuthResponse
import com.teumteumeat.teumteumeat.data.network.model_response.SocialLoginRequest
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.usecase.AutoLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val autoLoginUseCase: AutoLoginUseCase,
    private val socialLoginRepository: SocialLoginRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<LoginUiEvent>(
        replay = 0,              // 재전달 ❌
        extraBufferCapacity = 1  // 순간 이벤트 유실 방지
    )
    val uiEvent = _uiEvent.asSharedFlow()

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            // 🔄 로딩 시작
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = socialLoginRepository.socialLogin(
                provider = SocialProvider.GOOGLE.name,
                request = SocialLoginRequest(
                    idToken = idToken,
                    termsAgreed = false // 최초는 무조건 false
                ),
            )

            handleLoginResult(result)
            // 🔄 로딩 종료 (공통)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    private suspend fun handleLoginResult(result: ApiResultV2<AuthResponse?>) {
        when (result) {

            is ApiResultV2.Success -> {
                val data = result.data
                if (data == null) {
                    // ✅ 성공인데 data 없음 → 상태 에러
                    _uiState.update {
                        it.copy(errorMessage = "로그인 응답이 올바르지 않습니다.")
                    }
                    return
                }

                // ✅ 성공 이벤트
                _uiEvent.emit(
                    LoginUiEvent.LoginSuccess(
                        isOnboardingCompleted = data.isOnboardingCompleted
                    )
                )
            }

            is ApiResultV2.ServerError -> {
                when (result.code) {
                    "AUTH-006" -> {
                        // ✅ 약관 미동의 → 이벤트
                        _uiEvent.emit(LoginUiEvent.NeedTermsAgreement)
                    }
                    else -> {
                        // ❌ 서버 에러 → 상태
                        _uiState.update {
                            it.copy(errorMessage = result.uiMessage)
                        }
                    }
                }
            }

            is ApiResultV2.NetworkError -> {
                _uiState.update {
                    it.copy(errorMessage = "네트워크 오류가 발생했습니다.")
                }
            }

            is ApiResultV2.SessionExpired -> {
                _uiState.update {
                    it.copy(errorMessage = "세션이 만료되었습니다. 다시 로그인해주세요.")
                }
            }

            is ApiResultV2.UnknownError -> {
                _uiState.update {
                    it.copy(errorMessage = "알 수 없는 오류가 발생했습니다.")
                }
            }
        }
    }


}

sealed class LoginUiEvent{
    data object NeedTermsAgreement : LoginUiEvent()
    data class LoginSuccess(val isOnboardingCompleted: Boolean = false) : LoginUiEvent()
}

enum class SocialProvider {
    KAKAO, GOOGLE
}
