package com.teumteumeat.teumteumeat.ui.screen.a1_login

import com.teumteumeat.teumteumeat.ui.screen.a1_login.state.PendingSocialLogin
import com.teumteumeat.teumteumeat.ui.screen.a1_login.state.TermsAgreementState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState

data class LoginUiState(
    val isLoading: Boolean = false,        // 서버 통신용
    val isAuthBlocking: Boolean = false,   // ⭐ 구글/카카오 인증 대기용
    val errorMessage: String? = null,

    // ✅ 리그 공유 딥링크로 진입한 로그인/온보딩 흐름인지 여부 (로그인·온보딩을 모두 마친 뒤 리그로 보내야 함)
    val pendingLeagueRedirect: Boolean = false,
    // ✅ 위 상태로 로그인 화면에 도착했을 때: 도착 즉시 안내 팝업 노출 여부 (확인 누르면 false, pendingLeagueRedirect는 유지)
    val showPendingLeagueNotice: Boolean = false,


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
    
    val errorState: ErrorState = ErrorState(
        title = "",
        description = "",
        retryLabel = "",
        onRetry = {}
    )
)

sealed class KakaoLoginError {

    object UserCancelled : KakaoLoginError()

    object NetworkError : KakaoLoginError()

    object KakaoAppNotAvailable : KakaoLoginError()

    object AuthFailed : KakaoLoginError()

    data class Unknown(val throwable: Throwable) : KakaoLoginError()
}