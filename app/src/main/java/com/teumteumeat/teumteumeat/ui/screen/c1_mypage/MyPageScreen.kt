package com.teumteumeat.teumteumeat.ui.screen.c1_mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageAccountSection
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageArrowRow
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageNavigateBox
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageRow
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageToggleRow
import com.teumteumeat.teumteumeat.ui.component.mypage.SelectedTopicSection
import com.teumteumeat.teumteumeat.ui.screen.a1_login.SocialProvider
import com.teumteumeat.teumteumeat.ui.screen.c3_edit_user_info.EditUserInfoActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.Utils.UxUtils
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.modal.NotificationSettingGuideOverlay
import com.teumteumeat.teumteumeat.ui.screen.common_screen.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState

@Composable
fun MyPageScreen(
    uiState: UiStateMyPage,
    onBackClick: () -> Unit,
    onTopicClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAlarmToggle: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onCustomerCenterClick: () -> Unit,
    onWithdroawClick: () -> Unit,
    onNotificationGuideConfirm: () -> Unit,
    onNotificationGuideDismiss: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography
    val context = LocalContext.current
    val screenState = LocalScreenState.current

    DefaultMonoBg() {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            topBar = {
                TitleBar(
                    "내 정보",
                    onBackClick = onBackClick,
                )
            }
        ) { padding ->

            when (screenState) {
                is UiScreenState.Error -> {
                    FullScreenErrorModal(
                        errorState = ErrorState(
                            title = "문제가 발생했어요",
                            description = (screenState).message,
                            onRetry = onRetryClick,
                        ),
                        isShowBackBtn = false,
                        onBack = onBackClick
                    )
                }
                UiScreenState.Idle,
                UiScreenState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LoadingScreen(
                            title = "내 정보 불러오는 중",
                            message = "잠시만 기다려주세요"
                        )
                    }
                }

                UiScreenState.Success -> {
                    if (uiState.socialProvider != SocialProvider.NONE){
                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
                                .background(theme.backSurface)
                        ) {
                            Spacer(modifier = Modifier.height(5.dp))

                            /** ================= 학습 주제 ================= */
                            MyPageNavigateBox(
                                title = "학습 주제",
                                onClick = onTopicClick,
                                modifier = Modifier.background(
                                    color = theme.backgroundW100
                                )
                            ) {
                                SelectedTopicSection(
                                    topic = uiState.selectedTopic,
                                    description = uiState.topicDescription,
                                    goalWeek = uiState.goalWeek,
                                    difficulty = uiState.goalDifficulty,
                                    isSelGoalExpired = uiState.isSelGoalCompleted
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))


                            /** ================= 알림 설정 ================= */
                            MyPageToggleRow(
                                title = "알림 설정",
                                checked = uiState.isAlarmEnabled,
                                onCheckedChange = onAlarmToggle,
                                modifier = Modifier.background(
                                    color = theme.backgroundW100
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            /** ================= 틈틈잇 사용 설정 ================= */
                            MyPageRow(
                                title = "틈틈잇 사용 설정",
                                rightContent = {
                                    Row(
                                        modifier = Modifier.clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = {
                                                UxUtils.moveActivity(
                                                    context,
                                                    EditUserInfoActivity::class.java,
                                                    exitFlag = false,
                                                )
                                            }
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "전체 보기",
                                            style = MaterialTheme.appTypography.lableMedium12_h14,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            /** ================= 계정 정보 ================= */
                            MyPageAccountSection(
                                title = "계정 정보",
                                providerName = uiState.loginProvider,
                                email = uiState.email,
                                providerIcon = {
                                    Icon(
                                        painter = painterResource(uiState.socialProvider.toIconImg()),
                                        contentDescription = null,
                                        tint = Color.Unspecified
                                    )

                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            /** ================= 기타 ================= */
                            MyPageRow(
                                title = "기타",
                                textStyle = MaterialTheme.appTypography.bodySemiBold18
                            )
                            MyPageArrowRow(
                                title = "이용약관",
                                onClick = {
                                    UxUtils.openExternalBrowser(
                                        context,
                                        "https://resolute-flier-02d.notion.site/2d8151abb62e80cbaefde6ddcef603cc?pvs=74"
                                    )
                                }
                            )
                            MyPageArrowRow(
                                title = "고객센터",
                                onClick = {
                                    UxUtils.openExternalBrowser(
                                        context,
                                        "https://forms.gle/jmrRbST7XwLsfhoE7"
                                    )
                                }
                            )
                            MyPageRow(
                                title = "버전 정보",
                                rightContent = {
                                    Text(
                                        text = uiState.appVersion,
                                    )
                                }
                            )


                            Spacer(modifier = Modifier.height(200.dp))

                            /** ================= 하단 ================= */
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "로그아웃",
                                    style = typo.captionRegular12.copy(
                                        color = theme.textGhost
                                    ),

                                    modifier = Modifier.clickable {
                                        onLogoutClick()
                                    },

                                    )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    modifier = Modifier.clickable(
                                        onClick = onWithdroawClick
                                    ),
                                    text = "탈퇴하기",
                                    style = typo.captionRegular12.copy(
                                        color = theme.textGhost
                                    ),
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }

            NotificationSettingGuideOverlay(
                notificationGuideType = uiState.notificationGuideType,
                onConfirm = onNotificationGuideConfirm,
                onDismiss = onNotificationGuideDismiss
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMyPageScreenSuccess() {
    // 1. 프리뷰용 가짜 UI 상태 데이터
    val mockUiState = UiStateMyPage(
        selectedTopic = "안드로이드 개발",
        topicDescription = "Jetpack Compose 마스터하기",
        goalWeek = "4주",
        goalDifficulty = "상",
        isSelGoalCompleted = false,
        isAlarmEnabled = true,
        loginProvider = "카카오 로그인",
        email = "teumteum@example.com",
        socialProvider = SocialProvider.KAKAO,
        appVersion = "1.0.0"
    )

    TeumTeumEatTheme {
        CompositionLocalProvider(
            LocalScreenState provides UiScreenState.Error(message = ""), // 성공 상태로 고정
        ) {
            MyPageScreen(
                uiState = mockUiState,
                onBackClick = {},
                onTopicClick = {},
                onLogoutClick = {},
                onAlarmToggle = {},
                onTermsClick = {},
                onCustomerCenterClick = {},
                onWithdroawClick = {},
                onNotificationGuideConfirm = {},
                onNotificationGuideDismiss = {},
                onRetryClick = {}
            )
        }
    }
}




