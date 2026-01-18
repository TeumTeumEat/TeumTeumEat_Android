package com.teumteumeat.teumteumeat.ui.screen.c3_edit_user_info

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.component.BottomSheetContainerRightTopConfirm
import com.teumteumeat.teumteumeat.ui.component.MinuteRadioGroup
import com.teumteumeat.teumteumeat.ui.component.NoLableTextField
import com.teumteumeat.teumteumeat.ui.component.TimeSliderWithPickTime
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.toDisplayText
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditUserInfoScreen(
    uiState: UiStateEditUserInfo,
    onBackClick: () -> Unit,
    onInfoSaveClick: (Int) -> Unit,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography
    val viewModel = LocalViewModelContext.current as EditUserInfoViewModel

    val focusRequesterInput = remember { FocusRequester() } // 이름 인풋 필드 포커스 요청자
    val inputInteractionSource =
        remember { MutableInteractionSource() } // 사용자가 특정 UI 요소와 상호작용하고 있는지를 감지하는 객체
    val inputFocused by inputInteractionSource.collectIsFocusedAsState() // ✅ 포커스 여부 감지
    val focusManager = LocalFocusManager.current
    val isNameValid = uiState.isNameValid && uiState.errorMessage == ""
    val keyboardController = LocalSoftwareKeyboardController.current

    val activity = LocalActivityContext.current as EditUserInfoActivity

    LaunchedEffect(uiState) {
        Log.d(
            "COMPOSE_UI_STATE",
            """
        🎨 Compose received uiState
        workInTime=${uiState.workInTime}
        workOutTime=${uiState.workOutTime}
        useMinutes=${uiState.useMinutes}
        """.trimIndent()
        )
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = "back"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "틈틈잇 사용 설정",
                    style = MaterialTheme.appTypography.subtitleSemiBold20
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        },
        bottomBar = {
            // 하단 퀴즈 버튼
            BaseFillButton(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                onClick = {
                    // viewModel에서 현재 정보 post
                    viewModel.saveUserInfo()
                    activity.finish()
                },
                isEnabled = !isNameValid,
                text = "저장하기"
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null, // 물결 효과 제거
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
                .padding(padding)
                .padding(horizontal = 20.dp)
                .background(theme.backgroundW100)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 로딩 상태
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 40.dp)
                    )
                }
                return@Column
            }

            // 🔹 에러 상태
            uiState.errorMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        color = Color.Red,
                        modifier = Modifier
                            .padding(20.dp)
                    )
                    return@Column
                }
            }

            // 🔹 유저 앱이용 정보 리스트
            // 타이틀
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    "닉네임 설정",
                    style = Typography.bodyLarge.copy(
                        fontSize = 18.sp,
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 닉네임 입력 필드 (기존 컴포넌트 재사용)
            NoLableTextField(
                value = uiState.charName,
                placeholderText = "입력해주세요.",
                onValueChange = { input ->
                    viewModel.onNameTextChanged(input)
                },
                modifier = Modifier.fillMaxWidth(),
                isFocused = inputFocused,
                focusRequesterThis = focusRequesterInput,
                interactionSource = inputInteractionSource,
                isError = uiState.nameErrorMessage.isNotEmpty(),
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
                showCharCount = inputFocused
            )

            // 🔹 에러 메시지 (닉네임 전용)
            if (uiState.nameErrorMessage.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 8.dp)
                ) {
                    Text(
                        text = uiState.nameErrorMessage,
                        style = Typography.displaySmall,
                        color = if (uiState.nameErrorMessage.isNotEmpty())
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }else{
                Spacer(modifier = Modifier.height(30.dp))
            }

// 집에서 나오는 시간
            TimeSettingRow(
                title = "집에서 나오는 시간",
                timeText = uiState.workInTime.toDisplayText(isSelected = true),
                onClick = {
                    viewModel.openBottomSheet(BottomSheetType.WorkInTime)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

// 집에 돌아가는 시간
            TimeSettingRow(
                title = "집에 돌아가는 시간",
                timeText = uiState.workOutTime.toDisplayText(isSelected = true),
                onClick = {
                    viewModel.openBottomSheet(BottomSheetType.WorkOutTime)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

// 이용 시간
            TimeSettingRow(
                title = "이용 시간",
                timeText = "${uiState.useMinutes}분",

                onClick = {
                    viewModel.openBottomSheet(BottomSheetType.UsingTime)
                }
            )


            if (uiState.showBottomSheet) {
                BottomSheetContainerRightTopConfirm(
                    titleText = when (uiState.currentBottomSheetType) {
                        BottomSheetType.WorkInTime -> "집을 나오는 시간"
                        BottomSheetType.WorkOutTime -> "집으로 가는 시간"
                        BottomSheetType.UsingTime -> "이용 시간"
                        null -> ""
                    },
                    onDismiss = {
                        viewModel.closeBottomSheet()
                    },
                    onConfirm = {
                        viewModel.confirmBottomSheet()
                    },
                    onCompleteEnable = true,

                    content = {

                        when (uiState.currentBottomSheetType) {

                            BottomSheetType.WorkInTime,
                            BottomSheetType.WorkOutTime -> {
                                TimeSliderWithPickTime(
                                    state = uiState.tempTime,
                                    onChange = { viewModel.updateTempTime(it) }
                                )
                            }

                            BottomSheetType.UsingTime -> {
                                MinuteRadioGroup(
                                    modifier = Modifier.padding(top = 20.dp),
                                    options = listOf(5, 7, 10, 15),
                                    selectedMinute = uiState.tempUseMinutes,
                                    onSelect = { minute ->
                                        viewModel.updateTempUseMinutes(minute)
                                    }
                                )
                            }

                            null -> {}
                        }
                    }
                )
            }
        }

    }
}

@Composable
fun TimeSettingRow(
    title: String,
    timeText: String,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.appTypography.subtitleSemiBold16.copy(
                color = MaterialTheme.extendedColors.textSecondary
            ),
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White)
                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.appTypography.btnMedium18_h24
            )
        }
    }
}





