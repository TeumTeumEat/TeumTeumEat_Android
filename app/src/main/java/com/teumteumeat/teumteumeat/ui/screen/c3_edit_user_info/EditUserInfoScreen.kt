package com.teumteumeat.teumteumeat.ui.screen.c3_edit_user_info

import androidx.activity.compose.BackHandler
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teumteumeat.teumteumeat.ui.component.BottomSheetContainerRightTopConfirm
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.radio_group.MinuteRadioGroup
import com.teumteumeat.teumteumeat.ui.component.NoLableTextField
import com.teumteumeat.teumteumeat.ui.component.TimeSliderWithPickTime
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
import com.teumteumeat.teumteumeat.ui.component.modal.BaseModal
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
    onInfoSaveClick: () -> Unit,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography
    val viewModel = LocalViewModelContext.current as EditUserInfoViewModel

    val focusRequesterInput = remember { FocusRequester() } // 이름 인풋 필드 포커스 요청자
    val inputInteractionSource =
        remember { MutableInteractionSource() } // 사용자가 특정 UI 요소와 상호작용하고 있는지를 감지하는 객체
    val inputFocused by inputInteractionSource.collectIsFocusedAsState() // ✅ 포커스 여부 감지
    val focusManager = LocalFocusManager.current
    val isNameValid = uiState.isNameValid && uiState.errorMessage.isNullOrEmpty()
    val keyboardController = LocalSoftwareKeyboardController.current

    val isInfoChanged = with(uiState){
        !(originalCharName == charName &&
        originalWorkInTime == workInTime &&
        originalWorkOutTime == workOutTime &&
        originalUseMinutes == useMinutes)
    }

    val activity = LocalActivityContext.current as EditUserInfoActivity

    // 시스템 뒤로가기 버튼 처리
    BackHandler {
        if (isInfoChanged) {
            viewModel.checkUnsavedChanges()
        } else {
            onBackClick()
        }
    }

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

    DefaultMonoBg() {
        Scaffold(
            modifier = Modifier
                .systemBarsPadding(),
            containerColor = theme.backgroundW100,

            topBar = {
                TitleBar(
                    title = "틈틈잇 사용 설정",
                    onBackClick = {
                        if (isInfoChanged) {
                            viewModel.checkUnsavedChanges()
                        } else {
                            onBackClick()
                        }
                    },
                )
            },
            bottomBar = {
                // 하단 저장 버튼
                BaseFillButton(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    onClick = {
                        onInfoSaveClick()
                        activity.finish()
                    },
                    isEnabled = isNameValid && isInfoChanged,
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
                        text = "닉네임 설정",
                        style = MaterialTheme.appTypography.subtitleSemiBold16.copy(
                            color = MaterialTheme.extendedColors.textSecondary
                        ),
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
                } else {
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

                // 상태 변수가 true일 때 다이얼로그를 표시합니다.
                if (uiState.isShowSaveDialog) {
                    Dialog(
                        onDismissRequest = {
                            // 다이얼로그 바깥을 터치하거나 뒤로가기 버튼을 눌렀을 때 처리
                            viewModel.dismissConfirmationDialog()
                        },
                        properties = DialogProperties(
                            usePlatformDefaultWidth = false // 커스텀 패딩을 적용하기 위해 기본 너비 제한 해제
                        )
                    ) {
                        BaseModal(
                            title = "저장 확인",
                            body = "변경하신 사항을 저장하시겠습니까?",
                            secondaryButtonText = "뒤로가기",
                            primaryButtonText = "저장하기",
                            onSecondaryClick = {
                                // '뒤로가기' 클릭 시 동작: 저장하지 않고 뒤로 감
                                viewModel.dismissConfirmationDialog()
                                onBackClick()
                            },
                            onPrimaryClick = {
                                // '저장하기' 클릭 시 동작: 저장 후 뒤로 감
                                onInfoSaveClick()
                                viewModel.dismissConfirmationDialog()
                                activity.finish()
                            }
                        )
                    }
                }
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
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White)
                .clip(RoundedCornerShape(16.dp))
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





