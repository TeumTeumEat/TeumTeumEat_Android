package com.teumteumeat.teumteumeat.ui.screen.a1_login

import com.teumteumeat.teumteumeat.data.network.model_response.SocialLoginRequest
import com.teumteumeat.teumteumeat.ui.screen.a1_login.state.PendingSocialLogin
import com.teumteumeat.teumteumeat.ui.screen.a1_login.state.TermsAgreementState

data class LoginUiState(
    val isLoading: Boolean = false,        // 서버 통신용
    val isAuthBlocking: Boolean = false,   // ⭐ 구글/카카오 인증 대기용
    val errorMessage: String? = null,

    // ✅ 바텀시트 관련
    val bottomSheetType: LoginBottomSheetType = LoginBottomSheetType.NONE,
    val showBottomSheet: Boolean = false,

    // ✅ 약관 동의 상태
    val termsAgreement: TermsAgreementState = TermsAgreementState(),

    // ✅ 현재 진행 중인 소셜 로그인 정보
    val pendingSocialLogin: PendingSocialLogin = PendingSocialLogin(
        provider = SocialProvider.NONE,
        idToken = "",
        authCode = null,
    ),
)

sealed class KakaoLoginError {

    object UserCancelled : KakaoLoginError()

    object NetworkError : KakaoLoginError()

    object KakaoAppNotAvailable : KakaoLoginError()

    object AuthFailed : KakaoLoginError()

    data class Unknown(val throwable: Throwable) : KakaoLoginError()
}