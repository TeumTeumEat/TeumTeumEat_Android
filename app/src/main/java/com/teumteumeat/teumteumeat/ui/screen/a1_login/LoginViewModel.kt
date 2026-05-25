package com.teumteumeat.teumteumeat.ui.screen.a1_login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.AuthToken
import com.teumteumeat.teumteumeat.data.network.model.TokenLocalDataSource
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_request.AuthResponse
import com.teumteumeat.teumteumeat.data.network.model_response.SocialLoginRequest
import com.teumteumeat.teumteumeat.data.repository.login.SocialLoginRepository
import com.teumteumeat.teumteumeat.data.repository.user.UserRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.a1_login.state.PendingSocialLogin
import com.teumteumeat.teumteumeat.ui.screen.a1_login.state.TermsAgreementState
import com.teumteumeat.teumteumeat.utils.Utils.PrefsUtil
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
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
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val socialLoginRepository: SocialLoginRepository,
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val analyticsLogger: TeumAnalyticsLogger,
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<LoginUiEvent>(
        replay = 0,              // 재전달 ❌
        extraBufferCapacity = 1  // 순간 이벤트 유실 방지
    )
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        // 세션 만료 이벤트를 uiEvent로 전달 — Screen이 SessionManager를 직접 참조하지 않도록 캡슐화
        viewModelScope.launch {
            sessionManager.sessionEvent.collect {
                _uiEvent.emit(LoginUiEvent.NavigateToLogin)
            }
        }
    }

    /**
     * 💡 왜 일반 로딩과 분리 하였는가?
     * 인증 팝업 대기 ≠ 서버 통신
     * 나중에 UX 튜닝/로그 분석 시 훨씬 명확함
     */
    fun startAuthBlocking() {
        _uiState.update { it.copy(isAuthBlocking = true) }
    }

    fun endAuthBlocking() {
        _uiState.update { it.copy(isAuthBlocking = false) }
    }

    fun openTermsBottomSheet() {
        _uiState.update {
            it.copy(
                bottomSheetType = LoginBottomSheetType.TERMS_AGREEMENT,
                showBottomSheet = true
            )
        }
    }

    fun closeBottomSheet() {
        _uiState.update {
            it.copy(
                bottomSheetType = LoginBottomSheetType.NONE,
                showBottomSheet = false
            )
        }
    }

    fun onOver14Checked(checked: Boolean) {
        _uiState.update {
            it.copy(
                termsAgreement = it.termsAgreement.copy(over14 = checked)
            )
        }
    }

    fun onTermsOfServiceChecked(checked: Boolean) {
        _uiState.update {
            it.copy(
                termsAgreement = it.termsAgreement.copy(termsOfService = checked)
            )
        }
    }

    fun onPrivacyPolicyChecked(checked: Boolean) {
        _uiState.update {
            it.copy(
                termsAgreement = it.termsAgreement.copy(privacyPolicy = checked)
            )
        }
    }

    fun onAllTermsChecked(checked: Boolean) {
        _uiState.update {
            it.copy(
                termsAgreement = TermsAgreementState(
                    over14 = checked,
                    termsOfService = checked,
                    privacyPolicy = checked
                )
            )
        }
    }


    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            // 🔄 로딩 시작
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // ✅ 어떤 소셜 로그인인지 저장
            _uiState.update {
                it.copy(
                    pendingSocialLogin = PendingSocialLogin(
                        provider = SocialProvider.GOOGLE,
                        idToken = idToken
                    )
                )
            }

            requestSocialLogin(termsAgreed = false)
            // 🔄 로딩 종료 (공통)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loginWithKakaoServer(
        idToken: String,
        authCode: String?
    ) {
        viewModelScope.launch {

            // 1️⃣ pendingSocialLogin 세팅
            _uiState.update {
                it.copy(
                    isLoading = true,
                    pendingSocialLogin = PendingSocialLogin(
                        provider = SocialProvider.KAKAO,
                        idToken = idToken,
                        authCode = authCode // ✅ 추후 Apple 대비용 (현재 미사용)
                    )
                )
            }

            // 2️⃣ 서버 로그인 요청 (약관 미동의 상태)
            requestSocialLogin(termsAgreed = false)
        }
    }


    fun agreeTermsAndRegister() {
        // ✅ 어떤 소셜 로그인인지 ViewModel이 이미 알고 있음
        requestSocialLogin(termsAgreed = true)
    }

    private fun requestSocialLogin(termsAgreed: Boolean) {
        val pending = _uiState.value.pendingSocialLogin

        viewModelScope.launch {

            val result = socialLoginRepository.socialLogin(
                provider = pending.provider.name,
                request = SocialLoginRequest(
                    idToken = pending.idToken,
                    termsAgreed = termsAgreed,
                )
            )

            handleLoginResult(result)

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun handleLoginResult(result: ApiResultV2<AuthResponse?>) {
        Log.d("LoginDebug", "handleLoginResult called: $result")

        val method = _uiState.value.pendingSocialLogin.provider.name.lowercase()

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

                // 📊 login_complete: 소셜 로그인 성공
                analyticsLogger.logLoginComplete(method = method)

                saveAuthToken(data)

                // ✅ 로그인 성공 후 → 온보딩 여부 조회
                checkOnboardingCompleted()
            }

            is ApiResultV2.ServerError -> {
                when (result.code) {
                    "AUTH-006" -> {
                        // ✅ 약관 미동의 → 이벤트
                        // 📊 login_fail: 약관 미동의로 인한 실패 (error_message로 서버 메시지 기록)
                        analyticsLogger.logLoginFail(
                            method = method,
                            errorCode = result.code,
                            errorMessage = result.uiMessage,
                        )
                        _uiEvent.emit(LoginUiEvent.NeedTermsAgreement)
                    }

                    else -> {
                        // 📊 login_fail: 서버 에러 (error_message로 서버 메시지 기록)
                        analyticsLogger.logLoginFail(
                            method = method,
                            errorCode = result.code,
                            errorMessage = result.uiMessage,
                        )
                        // ❌ 서버 에러 → 상태
                        _uiState.update {
                            it.copy(errorMessage = result.uiMessage)
                        }
                    }
                }
            }

            is ApiResultV2.NetworkError -> {
                // 📊 login_fail: 네트워크 오류 (상세 메시지 불필요 — 연결 실패 자체가 원인)
                analyticsLogger.logLoginFail(method = method, errorCode = "NETWORK_ERROR")
                _uiState.update {
                    it.copy(errorMessage = result.uiMessage)
                }
            }

            is ApiResultV2.SessionExpired -> {
                // 📊 login_fail: 세션 만료
                analyticsLogger.logLoginFail(method = method, errorCode = "SESSION_EXPIRED")
                // _uiEvent.emit(LoginUiEvent.NavigateToLogin)
                _uiState.update {
                    it.copy(errorMessage = result.uiMessage)
                }
            }

            is ApiResultV2.UnknownError -> {
                // 📊 login_fail: 알 수 없는 오류
                // throwable_class: 어떤 예외 클래스인지 (예: UnknownHostException, SocketTimeoutException)
                // error_message: throwable 메시지 우선, 없으면 result.message
                analyticsLogger.logLoginFail(
                    method = method,
                    errorCode = "UNKNOWN_ERROR",
                    errorMessage = result.throwable?.message ?: result.message,
                    throwableClass = result.throwable?.javaClass?.simpleName,
                )
                _uiState.update {
                    it.copy(errorMessage = result.uiMessage)
                }
            }
        }
    }

    private suspend fun checkOnboardingCompleted() {
        when (val result = userRepository.getOnboardingCompletedV2()) {

            is ApiResultV2.Success -> {
                if (result.data.completed) {
                    PrefsUtil.setOnboardingCompleted(getApplication(context), true)
                    _uiEvent.emit(LoginUiEvent.NavigateToMain)
                } else {
                    _uiEvent.emit(LoginUiEvent.NavigateToOnboarding)
                }
            }
            is ApiResultV2.SessionExpired -> {
                _uiState.update {
                    it.copy(errorMessage = "세션이 만료되었습니다. 다시 로그인해주세요.")
                }
                sessionManager.expireSession()
            }

            is ApiResultV2.NetworkError -> {
                _uiState.update {
                    it.copy(errorMessage = "네트워크 상태를 확인하고, 다시 로그인 하세요.")
                }
            }
            is ApiResultV2.ServerError,
            is ApiResultV2.UnknownError -> {
                // 🔴 실패 시 보수적으로 화면 이동 X
                _uiState.update {
                    it.copy(errorMessage = "서버에러 발생하였습니다. 다시 로그인해주세요.")
                }
            }


        }
    }

    private fun saveAuthToken(data: AuthResponse) {
        tokenLocalDataSource.save(
            AuthToken(
                accessToken = data.accessToken,
                refreshToken = data.refreshToken,
                socialLoginType = uiState.value.pendingSocialLogin.provider.name,
            )
        )
    }

    fun withdrawUser() {
        viewModelScope.launch {
            when (val result = socialLoginRepository.withdrawUser()) {
                is ApiResultV2.Success -> {
                    // ✅ 1. 로컬 토큰 삭제
                    tokenLocalDataSource.clear()

                    // ✅ 2. 로그인 화면으로 이동
                    _uiEvent.emit(LoginUiEvent.NavigateToLogin)
                }

                is ApiResultV2.ServerError -> {
                    _uiState.update { it.copy(errorMessage = result.uiMessage) }
                }

                else -> {
                    _uiState.update { it.copy(errorMessage = result.uiMessage) }
                }
            }
        }
    }

    fun setAuthErrorPage(errMsg: String) {
        _uiState.update {
            it.copy(
                errorMessage = errMsg,
            )
        }
    }

    fun setCurrentPendingLogin(provider: SocialProvider){
        _uiState.update {
            it.copy(
                pendingSocialLogin = it.pendingSocialLogin.copy(
                    provider = provider
                )
            )
        }
    }


}

sealed class LoginUiEvent {
    // 화면 이동
    data object NavigateToMain : LoginUiEvent()
    data object NavigateToOnboarding : LoginUiEvent()
    data object NavigateToLogin : LoginUiEvent()

    // 기타
    data object NeedTermsAgreement : LoginUiEvent()
}

enum class SocialProvider {
    KAKAO, GOOGLE, NONE
}

enum class LoginBottomSheetType {
    NONE,
    TERMS_AGREEMENT
}

