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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.NoLableTextField
import com.teumteumeat.teumteumeat.ui.theme.Typography


@Composable
fun OnBoardingSetCharNameScreen(
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .focusable() // ⭐ 포커스 받을 수 있는 영역 ,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))
                    Text(
                        "널 뭐라고 불러줄까?",
                        style = Typography.headlineMedium.copy(
                            fontSize = 18.sp,
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Image(
                        painter = painterResource(R.drawable.character_front),
                        contentDescription = "앞을 보는 케릭터",
                        modifier = Modifier.size(width = 200.dp, height = 162.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(56.dp))
                    NoLableTextField(
                        value = uiState.charName,
                        labelText = "",
                        placeholderText = "입력해주세요.",
                        onValueChange = { input ->
                            viewModel.onNameTextChanged(input)// viewModel set 함수 위치
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isFocused = inputFocused,
                        focusRequesterThis = focusRequesterInput,
                        interactionSource = inputInteractionSource,
                        isError = uiState.charName.isNotEmpty() && uiState.errorMessage != "",
                        onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide() // ⭐ 핵심
                        }
                    )
                    if(uiState.charName.isNotEmpty()){
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, start = 8.dp),
                        ){
                            Text(
                                text = uiState.errorMessage,
                                style = Typography.displaySmall.copy(
                                    color = if(uiState.errorMessage != "") MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.tertiary
                                ),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
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
                        }
                    )
                }
            }
        },
    )
}



@Preview(showBackground = true)
@Composable
fun OnBoardingInputNamePreview() {

    /*val fakeViewModel = remember { OnBoardingViewModel() }
    TeumTeumEatTheme {
        OnBoardingSetCharNameScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateOnBoardingMain(errorMessage = "한글 또는 영문만 입력할 수 있어요", isValid = false),
            onNext = {},
            onPrev = {}
        )
    }*/
}