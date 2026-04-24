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
import com.onesignal.OneSignal
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.component.CheckBoxCircle
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.TimeSliderWithPickTime
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.ui.theme.Typography
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.teumteumeat.teumteumeat.ui.component.BottomSheetContainerRightTopConfirm
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.component.radio_group.QuestionCountRadioGroup
import com.teumteumeat.teumteumeat.utils.Utils.UiUtils.areAppNotificationsEnabled
import com.teumteumeat.teumteumeat.utils.Utils.UiUtils.isPostNotificationsGranted
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun OnBoardingSetRoutineScreen(
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
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
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = if (Build.VERSION.SDK_INT >= 33) {
                    isPostNotificationsGranted(context) && areAppNotificationsEnabled(context)
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
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.requestNotificationPermission) {
        if (uiState.requestNotificationPermission) {
            OneSignal.Notifications.requestPermission(false)
            viewModel.consumeNotificationPermissionRequest()
        }
    }

    OnBoardingSetRoutineScreenContent(
        uiState = uiState,
        onOpenWorkInTimeSheet = { viewModel.openTimeSheet(TimeType.IN) },
        onOpenWorkOutTimeSheet = { viewModel.openTimeSheet(TimeType.OUT) },
        onNotificationOptionClicked = { viewModel.onNotificationOptionClicked() },
        onTimeChanged = { viewModel.onTimeChanged(it) },
        onCloseTimeSheet = { viewModel.closeTimeSheet() },
        onConfirmTime = { viewModel.confirmTime(); viewModel.closeTimeSheet() },
        onOpenNotificationSetting = { viewModel.openNotificationSetting() },
        onCloseNotificationGuide = { viewModel.closeNotificationSettingGuide() },
        onNext = onNext,
        onMinuteSelected = { questionCount -> viewModel.onMinuteSelected(questionCount) },
    )
}

@Composable
private fun OnBoardingSetRoutineScreenContent(
    uiState: UiStateOnboardingState,
    onOpenWorkInTimeSheet: () -> Unit,
    onOpenWorkOutTimeSheet: () -> Unit,
    onNotificationOptionClicked: () -> Unit,
    onTimeChanged: (TimeState) -> Unit,
    onCloseTimeSheet: () -> Unit,
    onConfirmTime: () -> Unit,
    onOpenNotificationSetting: () -> Unit,
    onCloseNotificationGuide: () -> Unit,
    onMinuteSelected: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val density = LocalDensity.current
    var bottomCardHeightDp by remember { mutableStateOf(0.dp) }

    val isSetAllTimeValid = uiState.isSetFirstAlarmTime && uiState.isSetSecondAlarmTime
    val sheetTitle = when (uiState.currentTimeType) {
        TimeType.IN -> "1번째 알림"
        TimeType.OUT -> "2번째 알림"
        TimeType.NOTTING -> "시간"
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
                        text = "나만의 퀴즈 루틴을 만들어요!\n몇 문제를 몇 시에 풀지 정해볼까요?",
                    )
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_two),
                        contentDescription = "운동하고 있는 케릭터",
                        modifier = Modifier.size(width = 200.dp, height = 200.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text("학습 분량", style = MaterialTheme.appTypography.subtitleSemiBold16)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    QuestionCountRadioGroup(
                        options = listOf(3, 5, 7, 10),
                        selectedQuestionCnt = uiState.selectedMinute,
                        onSelect = { questionCount ->
                            onMinuteSelected(questionCount)
                        }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text("알림 시간", style = MaterialTheme.appTypography.subtitleSemiBold16)
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text(
                            "* 학습을 이어갈 수 있도록 하루에 최대 2번까지 알람을 보내드려요",
                            style = MaterialTheme.appTypography.captionRegular12.copy(
                                color = MaterialTheme.extendedColors.textGhost
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    BaseOutlineButton(
                        text = "1번째 알림",
                        textStyle = MaterialTheme.appTypography.captionRegular12.copy(
                            color = MaterialTheme.extendedColors.textSecondary
                        ),
                        subText = uiState.workInTime.toDisplayText(isSelected = uiState.isSetFirstAlarmTime),
                        subTextStyle = MaterialTheme.appTypography.btnMedium18_h24.copy(
                            color = if (uiState.isSetFirstAlarmTime) MaterialTheme.extendedColors.textSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onOpenWorkInTimeSheet,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BaseOutlineButton(
                        text = "2번째 알림",
                        textStyle = MaterialTheme.appTypography.captionRegular12.copy(
                            color = MaterialTheme.extendedColors.textSecondary
                        ),
                        subText = uiState.workOutTime.toDisplayText(isSelected = uiState.isSetSecondAlarmTime),
                        subTextStyle = MaterialTheme.appTypography.btnMedium18_h24.copy(
                            color = if (uiState.isSetSecondAlarmTime) MaterialTheme.extendedColors.textSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onOpenWorkOutTimeSheet,
                    )

                    Spacer(Modifier.height(bottomCardHeightDp))
                }

                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color.White)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                        .onSizeChanged { size ->
                            bottomCardHeightDp = with(density) { size.height.toDp() + 62.dp}
                        }
                        .padding(top = 10.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CheckBoxCircle(
                            checked = uiState.isNotificationChecked,
                            onCheckedChange = { onNotificationOptionClicked() }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "해당 시간에 알림을 받으실건가요? (필수)",
                            style = MaterialTheme.appTypography.captionRegular14.copy(
                                color = MaterialTheme.extendedColors.textGhost
                            ),
                            modifier = Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                onNotificationOptionClicked()
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BaseFillButton(
                        text = "다음으로",
                        textStyle = Typography.labelMedium.copy(lineHeight = 24.sp),
                        isEnabled = isSetAllTimeValid && uiState.isNotificationChecked,
                        onClick = onNext,
                        conerRadius = 16.dp
                    )
                }

                if (uiState.showBottomSheet) {
                    BottomSheetContainerRightTopConfirm(
                        onDismiss = onCloseTimeSheet,
                        onConfirm = onConfirmTime,
                        titleText = sheetTitle,
                        content = {
                            TimeSliderWithPickTime(
                                state = uiState.tempTime,
                                onChange = onTimeChanged,
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


@Preview(showBackground = true)
@Composable
fun OnBoardingSetApptimeScreenPreview() {
    TeumTeumEatTheme {
        OnBoardingSetRoutineScreenContent(
            uiState = UiStateOnboardingState(),
            onOpenWorkInTimeSheet = {},
            onOpenWorkOutTimeSheet = {},
            onNotificationOptionClicked = {},
            onTimeChanged = {},
            onCloseTimeSheet = {},
            onConfirmTime = {},
            onOpenNotificationSetting = {},
            onCloseNotificationGuide = {},
            onNext = {},
            onMinuteSelected = {},
        )
    }
}