package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onesignal.OneSignal
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.component.CheckBoxCircle
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.TimeSliderWithPickTime
import com.teumteumeat.teumteumeat.ui.component.modal.BaseModal
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.ui.theme.Typography
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.teumteumeat.teumteumeat.ui.component.BottomSheetContainerRightTopConfirm
import com.teumteumeat.teumteumeat.ui.component.SpeechBubble
import com.teumteumeat.teumteumeat.utils.Utils.UiUtils.areAppNotificationsEnabled
import com.teumteumeat.teumteumeat.utils.Utils.UiUtils.isPostNotificationsGranted
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun OnBoardingSetApptimeScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    val isSetAllTimeValid = uiState.isSetWorkInTime && uiState.isSetWorkOutTime

    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = OneSignal.Notifications.permission
                Log.d("NotificationDebug", "ON_RESUME granted=$granted")
                viewModel.syncNotificationPermission(granted)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    /**
     * 🔹 화면 최초 진입 시
     * 🔹 권한 상태만 확인 (팝업 ❌)
     */
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

                val granted = if (Build.VERSION.SDK_INT >= 33) {
                    isPostNotificationsGranted(context) &&
                            areAppNotificationsEnabled(context)
                } else {
                    areAppNotificationsEnabled(context)
                }

                Log.d(
                    "NotificationDebug",
                    "ON_RESUME osGranted=$granted, oneSignal=${OneSignal.Notifications.permission}"
                )

                viewModel.syncNotificationPermission(granted)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 🔔 권한 팝업은 "이 상태가 true일 때만" 실행
    LaunchedEffect(uiState.requestNotificationPermission) {
        if (uiState.requestNotificationPermission) {
             OneSignal.Notifications.requestPermission(false)
            viewModel.consumeNotificationPermissionRequest()
        }
    }

    DefaultMonoBg(
        color = MaterialTheme.colorScheme.surface,
        extensionHeight = 0.dp,
        content = {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpeechBubble(
                        text = "대중교통을 이용하시는 시간에\n" +
                                "알림을 보내드릴게요",
                    )
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_two),
                        contentDescription = "앞을 보는 케릭터",
                        modifier = Modifier.size(width = 200.dp, height = 162.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(21.dp))

                    // 출근시간 박스
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ){
                        Text(
                            "집에서 나오는 시간",
                            style = MaterialTheme.appTypography.subtitleSemiBold16
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    BaseOutlineButton(
                        text = uiState.workInTime.toDisplayText(isSelected = uiState.isSetWorkInTime),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textStyle = MaterialTheme.appTypography.btnMedium18_h24.copy(
                            color = if(uiState.isSetWorkInTime) MaterialTheme.extendedColors.textSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            viewModel.openTimeSheet(TimeType.IN)
                        }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // 퇴근시간 박스
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ){
                        Text(
                            "집에 돌아가는 시간",
                            style = MaterialTheme.appTypography.subtitleSemiBold16
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    BaseOutlineButton(
                        text = uiState.workOutTime.toDisplayText(isSelected = uiState.isSetWorkOutTime),
                        textStyle = MaterialTheme.appTypography.btnMedium18_h24.copy(
                            color = if(uiState.isSetWorkOutTime) MaterialTheme.extendedColors.textSecondary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = {
                            viewModel.openTimeSheet(TimeType.OUT)
                        }
                    )

                    Spacer(modifier = Modifier.height(66.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ){
                        CheckBoxCircle(
                            checked = uiState.isNotificationChecked,
                            onCheckedChange = { checked ->
                                viewModel.onNotificationOptionClicked()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "해당 시간에 알림을 받으실건가요? (필수)",
                            style = MaterialTheme.appTypography.captionRegular14.copy(
                                color = MaterialTheme.extendedColors.textGhost
                            )
                        )
                    }

                    Spacer(Modifier.height(100.dp))
                }

                // 2️⃣ 하단 그라데이션 (페이드 효과)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    /*Button(onClick = {
                        Utils.PrefsUtil.clearNotificationDeniedOnce(context)
                    }) {
                        Text("알림 권한 이력 초기화 (DEBUG)")
                    }*/

                    BaseFillButton(
                        text = "다음으로",
                        textStyle = Typography.labelMedium.copy(
                            lineHeight = 24.sp
                        ),
                        isEnabled = isSetAllTimeValid && uiState.isNotificationChecked,
                        onClick = {
                            onNext()
                        },
                        conerRadius = 16.dp
                    )
                }


                // 🔹 바텀시트
                if (uiState.showBottomSheet) {
                    BottomSheetContainerRightTopConfirm(
                        onDismiss = {
                            viewModel.closeTimeSheet()
                        },
                        onConfirm = {
                            viewModel.confirmTime()
                            viewModel.closeTimeSheet()
                        },
                        titleText = viewModel.getSheetTitle(),
                        content = {
                            TimeSliderWithPickTime(
                                state = uiState.tempTime,
                                onChange = {
                                    viewModel.onTimeChanged(it)
                                }
                            )
                            Spacer(Modifier.height(20.dp))
                        },
                        onCompleteEnable = true,
                    )
                }
            }
        },
    )
}


@Composable
fun NotificationSettingGuideOverlay(
    uiState: UiStateOnboardingState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val type = uiState.notificationGuideType
    if (type == NotificationSettingGuideType.NONE) return

    // ✅ 타입에 따라 문구/타이틀 분기
    val (title, body, primary, secondary) = when (type) {
        NotificationSettingGuideType.ENABLE -> {
            Quad(
                "알림을 켜려면 설정이 필요해요",
                "알림 권한이 꺼져 있어요.\n기기 설정에서 알림을 허용해주세요.",
                "설정 화면",
                "취소"
            )
        }

        NotificationSettingGuideType.DISABLE -> {
            Quad(
                "알림을 끄려면 설정이 필요해요",
                "알림은 앱에서 직접 끌 수 없어요.\n기기 설정에서 변경할 수 있어요.",
                "설정 화면",
                "취소"
            )
        }

        NotificationSettingGuideType.NONE -> {
            // 여기로 올 일 없음
            Quad("", "", "", "")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        // 1) Dim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        )

        // 2) Center modal
        Box(modifier = Modifier.align(Alignment.Center)) {
            BaseModal(
                title = title,
                body = body,
                primaryButtonText = primary,
                secondaryButtonText = secondary,
                onPrimaryClick = onConfirm,
                onSecondaryClick = onDismiss
            )
        }
    }
}

/**
 * 간단히 4개 값을 묶기 위한 helper (data class 사용)
 */
private data class Quad(
    val first: String,
    val second: String,
    val third: String,
    val fourth: String
)





@Preview(showBackground = true)
@Composable
fun OnBoardingSetApptimeScreenPreview() {

    val fakeViewModel : OnBoardingViewModel = hiltViewModel()
    TeumTeumEatTheme {
        OnBoardingSetApptimeScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateOnboardingState(errorMessage = "한글 또는 영문만 입력할 수 있어요", isNameValid = false),
            onNext = {},
            onPrev = {}
        )
    }
}