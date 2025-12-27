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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.BottomSheetContainer
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.NoLableMultiLineTextField
import com.teumteumeat.teumteumeat.ui.component.TextRadioGroup
import com.teumteumeat.teumteumeat.ui.component.WeekRadioGroup
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

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
    val isPromptValid = uiState.promptInput.length <= 30
            uiState.isDiffculty.isNotEmpty()
    val keyboardController = LocalSoftwareKeyboardController.current


    // ✅ (1) 스크롤 & bring-into-view 준비
    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val difficultyOptions = listOf(
        DifficultyOption("상", 3),
        DifficultyOption("중", 2),
        DifficultyOption("하", 1),
    )

    DefaultMonoBg(
        color = MaterialTheme.colorScheme.surface,
        content = {
            Scaffold(
                modifier = Modifier.fillMaxSize()
                    .focusable(), // ⭐ 포커스 받을 수 있는 영역,
                containerColor = Color.Transparent,

                // ✅ (2) bottomBar로 버튼 고정 + 키보드 위로 올리기
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.ime) // ✅ 키보드 위로
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp)
                            .padding(horizontal = 20.dp),
                    ) {
                        BaseFillButton(
                            text = "다음으로",
                            textStyle = MaterialTheme.appTypography.btnBold20_h24.copy(
                                color = MaterialTheme.extendedColors.backgroundW100
                            ),
                            isEnabled = isPromptValid,
                            onClick = {
                                viewModel.onConfirmClick()
                                onNext()
                            },
                            conerRadius = 16.dp
                        )
                    }
                },
                content = { innerPadding ->
                    Column(
                        modifier = Modifier.Companion
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp)
                            .verticalScroll(scrollState) // ✅ 입력 중 스크롤 가능
                            .padding(bottom = 120.dp), // ✅ 하단 버튼 공간
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                "난이도를 선택해주세요",
                                style = MaterialTheme.appTypography.bodySemiBold18
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        BaseOutlineButton(
                            text = if (uiState.isDiffculty.isNotEmpty()) uiState.isDiffculty
                            else "난이도를 선택해주세요.",
                            textStyle = Typography.titleSmall.copy(
                                fontSize = 18.sp,
                                lineHeight = 24.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = {
                                viewModel.openBottomSheet(BottomSheetType.DIFFICULTY)
                            }
                        )
                        Spacer(Modifier.height(30.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                "퀴즈 설정을 위한 프롬프트를 입력해보세요.",
                                style = MaterialTheme.appTypography.bodySemiBold18
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        NoLableMultiLineTextField(
                            value = uiState.promptInput,
                            labelText = "",
                            placeholderText = "상황설정 예시가 필요합니다.\n" +
                                    "어떤 식으로 할지 어떤 상황인지 입력해주세요\n" +
                                    "ex) IT 트렌드나 프로그래밍 관련 퀴즈를 \n풀고 싶어요",
                            onValueChange = { input ->
                                viewModel.onPromptInputChanged(input)// viewModel set 함수 위치
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(bringIntoViewRequester),
                            isFocused = inputFocused,
                            focusRequesterThis = focusRequesterInput,
                            interactionSource = inputInteractionSource,
                            isError = uiState.promptInput.length > 30, // 30자 이상 초과
                        )
                        if (uiState.promptInput.length > 30) {
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



                    // 🔹 바텀시트
                    if (uiState.showBottomSheet) {
                        BottomSheetContainer(
                            titleText = "난이도를 선택해주세요",
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
            )

        },
    )
}
