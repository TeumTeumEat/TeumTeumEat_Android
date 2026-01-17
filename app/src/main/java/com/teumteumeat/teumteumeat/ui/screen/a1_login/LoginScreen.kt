package com.teumteumeat.teumteumeat.ui.screen.a1_login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.BottomSheetContainerRightTopConfirm
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.component.loading.FullScreenLoading
import com.teumteumeat.teumteumeat.ui.component.login.TermsAgreementBottomSheetContent
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.Utils.UxUtils
import com.teumteumeat.teumteumeat.utils.Utils.UxUtils.moveActivity
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import kotlin.jvm.java

@Composable
fun LoginScreen(
    onKakaoLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivityContext.current as LoginActivity
    val shape = RoundedCornerShape(28.dp)
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography
    val loginButtonShape = RoundedCornerShape(12.dp)

    // 🔥 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {

                LoginUiEvent.NeedTermsAgreement -> {
                    Log.d("Login", "navigate terms")
                    viewModel.openTermsBottomSheet()
                }

                LoginUiEvent.NavigateToOnboarding -> {
                    Log.d("Login", "navigate onboarding")
                    moveActivity(activity, OnBoardingActivity::class.java, exitFlag = true)
                }

                LoginUiEvent.NavigateToMain -> {
                    Log.d("Login", "navigate Main")
                    moveActivity(activity, MainActivity::class.java, exitFlag = true)
                }

                LoginUiEvent.NavigateToLogin -> {
                    Log.d("Login", "return to login")
                }
            }
        }
    }

    // ❌ 에러 표시
    uiState.errorMessage?.let { message ->
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    TeumTeumEatTheme {
        // 🔄 로딩 표시
        if (uiState.isLoading) {
            FullScreenLoading()
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "개인 맞춤형 퀴즈 기반 학습 서비스",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo_login),
                    contentDescription = "메인 로고",
                    contentScale = ContentScale.Fit
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 70.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (BuildConfig.DEBUG) {
                    BaseOutlineButton(
                        modifier = Modifier.padding(20.dp),
                        text = "회원탈퇴",
                        textStyle = typo.bodyMedium14_20.copy(
                            color = theme.error
                        ),
                        isEnabled = true,
                        onClick = {
                            // 회원탈퇴 기능 구현
                            viewModel.withdrawUser()
                        },
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            color = Color(0xFFFEE500),
                            shape = loginButtonShape
                        )
                        .clip(loginButtonShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Log.d("버튼 탭: ", "카카오 로그인 버튼")
                            onKakaoLoginClick()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {

                        // 🔹 카카오 로고
                        Image(
                            painter = painterResource(id = R.drawable.icon_kakao_talk),
                            contentDescription = "KakaoTalk Logo",
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // 🔹 텍스트
                        Text(
                            text = "카카오계정 로그인",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF121212)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            color = MaterialTheme.extendedColors.backgroundW100,
                            shape = loginButtonShape
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = loginButtonShape
                        )
                        .clip(loginButtonShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Log.d("버튼 탭: ", "구글 로그인 버튼")
                            onGoogleLoginClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {

                        // 🔹 구글 로고
                        Image(
                            painter = painterResource(id = R.drawable.icon_google),
                            contentDescription = "Google Logo",
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // 🔹 텍스트
                        Text(
                            text = "구글계정 로그인",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF121212)
                        )
                    }
                }
            }


            if (uiState.showBottomSheet &&
                uiState.bottomSheetType == LoginBottomSheetType.TERMS_AGREEMENT
            ) {
                BottomSheetContainerRightTopConfirm(
                    titleText = "이용약관",
                    onConfirm = {
                        viewModel.closeBottomSheet()
                        viewModel.agreeTermsAndRegister() // termsAgreed=true 재요청
                    },
                    onDismiss = {
                        viewModel.closeBottomSheet()
                    },
                    onCompleteEnable = uiState.termsAgreement.allRequiredAgreed,
                    content = {
                        TermsAgreementBottomSheetContent(
                            uiState = uiState,
                            onOver14Checked = viewModel::onOver14Checked,
                            onTermsOfServiceChecked = viewModel::onTermsOfServiceChecked,
                            onPrivacyPolicyChecked = viewModel::onPrivacyPolicyChecked,
                            onAllChecked = viewModel::onAllTermsChecked,
                            onConfirm = {
                                viewModel.closeBottomSheet()
                                viewModel.agreeTermsAndRegister() // termsAgreed=true 재요청
                            },
                            onGoServiceAgreeWebView = {
                                UxUtils.openExternalBrowser(
                                    activity,
                                    "https://resolute-flier-02d.notion.site/2d8151abb62e80cbaefde6ddcef603cc?pvs=74"
                                )
                            },
                            onGoPrivacyPolicyWebView = {
                                UxUtils.openExternalBrowser(
                                    activity,
                                    "https://resolute-flier-02d.notion.site/2d8151abb62e8099bfd6d881256a6b4a?source=copy_link"
                                )
                            }
                        )
                    },
                    tittleBottomPadding = 24
                )
            }

            // ✅ DEBUG + errorMessage 있을 때만 에러 모달
            if (BuildConfig.DEBUG && !uiState.errorMessage.isNullOrBlank()) {
                FullScreenErrorModal(
                    errorState = ErrorState(
                        title = "로그인 에러 발생",
                        description = uiState.errorMessage,
                        retryLabel = "다시 시도",
                        onRetry = {
                            if(uiState.pendingSocialLogin.provider == SocialProvider.KAKAO){
                                onKakaoLoginClick()
                            }else{
                                onGoogleLoginClick()
                            }
                        }
                    ),
                    isShowBackBtn = true,
                    onBack = {}
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val fakeViewModel: LoginViewModel = hiltViewModel()
    TeumTeumEatTheme {
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            LoginScreen(viewModel = fakeViewModel, onKakaoLoginClick = {}, onGoogleLoginClick = {})
        }
    }
}

