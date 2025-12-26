package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.BottomSheetContainer
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.MinuteRadioGroup
import com.teumteumeat.teumteumeat.ui.component.NoLableTextField
import com.teumteumeat.teumteumeat.ui.component.TextRadioGroup
import com.teumteumeat.teumteumeat.ui.component.TimeSliderWithPickTime
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.appTypography

@Composable
fun OptimizerDataScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnBoardingMain,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    val focusRequesterInput = remember { FocusRequester() } // 이름 인풋 필드 포커스 요청자
    val inputInteractionSource =
        remember { MutableInteractionSource() } // 사용자가 특정 UI 요소와 상호작용하고 있는지를 감지하는 객체
    val inputFocused by inputInteractionSource.collectIsFocusedAsState() // ✅ 포커스 여부 감지
    val focusManager = LocalFocusManager.current
    val isNameValid = uiState.isNameValid && uiState.errorMessage == ""
    val keyboardController = LocalSoftwareKeyboardController.current

    DefaultMonoBg(
        color = MaterialTheme.colorScheme.surface,
        content = {
            Box(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .focusable() // ⭐ 포커스 받을 수 있는 영역 ,
            ) {
                Column(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .padding(),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.Companion.height(60.dp))
                    Text(
                        "어떤 공부를 하고싶어?",
                        style = Typography.headlineMedium.copy(
                            fontSize = 18.sp,
                        )
                    )
                    Spacer(modifier = Modifier.Companion.height(20.dp))
                    Image(
                        painter = painterResource(R.drawable.character_front),
                        contentDescription = "앞을 보는 케릭터",
                        modifier = Modifier.Companion.size(width = 200.dp, height = 162.dp),
                        contentScale = ContentScale.Companion.Fit,
                    )
                    Spacer(modifier = Modifier.Companion.height(56.dp))
                    // todo. 난이도 입력 텍스트 + 버튼 구현
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ){
                        Text(
                            "난이도를 선택해주세요",
                            style = MaterialTheme.appTypography.bodySemiBold18
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    BaseOutlineButton(
                        text = uiState.isDiffculty?: "난이도를 선택해주세요.",
                        textStyle = Typography.titleSmall.copy(
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = {
                            viewModel.openBottomSheet(BottomSheetType.DIFFICULTY)
                        }
                    )

                    NoLableTextField(
                        value = uiState.charName,
                        labelText = "",
                        placeholderText = "입력해주세요.",
                        onValueChange = { input ->
                            viewModel.onNameTextChanged(input)// viewModel set 함수 위치
                        },
                        modifier = Modifier.Companion.fillMaxWidth(),
                        isFocused = inputFocused,
                        focusRequesterThis = focusRequesterInput,
                        interactionSource = inputInteractionSource,
                        isError = uiState.charName.isNotEmpty() && uiState.errorMessage != "",
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide() // ⭐ 핵심
                        }
                    )
                    if (uiState.charName.isNotEmpty()) {
                        Row(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(top = 10.dp, start = 8.dp),
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                style = Typography.displaySmall.copy(
                                    color = if (uiState.errorMessage != "") MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.tertiary
                                ),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = Typography.labelMedium.copy(
                            lineHeight = 24.sp
                        ),
                        isEnabled = isNameValid,
                        onClick = {
                            viewModel.onConfirmClick()
                            onNext()
                        },
                        conerRadius = 16.dp
                    )
                }

                // 🔹 바텀시트
                if (uiState.showBottomSheet) {
                    BottomSheetContainer(
                        titleText =  "난이도를 선택해주세요",
                        onDismiss = {
                            viewModel.closeBottomSheet()
                        }
                    ) {
                        TextRadioGroup(
                            options = listOf("상", "중", "하"),
                            selectedOption = uiState.isDiffculty,
                            onSelect = {
                                viewModel.onDifficultySelected(it)
                            },
                        )
                    }
                }
            }
        },
    )
}
