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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
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
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.ui.component.BottomSheetContainerRightTopConfirm
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.RequestPromptOptionList
import com.teumteumeat.teumteumeat.ui.component.radio_group.DifficultyRadioGroup
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun OptimizeDataScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onCloseSheet: () -> Unit,
    onConfirmPrompt: () -> Unit,
    setSheetTitle: String,
    onOpenPromptSheet: () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    val focusRequesterInput = remember { FocusRequester() } // 이름 인풋 필드 포커스 요청자
    val inputInteractionSource =
        remember { MutableInteractionSource() } // 사용자가 특정 UI 요소와 상호작용하고 있는지를 감지하는 객체
    val inputFocused by inputInteractionSource.collectIsFocusedAsState() // ✅ 포커스 여부 감지
    val focusManager = LocalFocusManager.current

    val isPromptValid = uiState.promptInput.length <= 30 &&
            uiState.difficulty != Difficulty.NONE
    val isPromptSelected = uiState.promptInput.isNotBlank()
    val keyboardController = LocalSoftwareKeyboardController.current


    // ✅ (1) 스크롤 & bring-into-view 준비
    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    val difficultyOptions = listOf(
        DifficultyOption("상", Difficulty.HARD),
        DifficultyOption("중", Difficulty.MEDIUM),
        DifficultyOption("하", Difficulty.EASY),
    )

    DefaultMonoBg(
        extensionHeight = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        content = {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
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
                                viewModel.setUserName()
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
                        Spacer(modifier = Modifier.Companion.height(4.dp))
                        SpeechBubble(text = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!")
                        Image(
                            painter = painterResource(R.drawable.char_onboarding_five_three),
                            contentDescription = "앞을 보는 케릭터",
                            contentScale = ContentScale.Companion.Fit,
                        )
                        Spacer(modifier = Modifier.Companion.height(15.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                "퀴즈 난이도 설정",
                                style = MaterialTheme.appTypography.subtitleSemiBold16
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        DifficultyRadioGroup(
                            options = difficultyOptions,
                            selected = uiState.difficulty,
                            onSelect = { difficulty ->
                                viewModel.onDifficultySelected(difficulty)
                            }
                        )

                        Spacer(Modifier.height(30.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                "요청 프롬프트 (선택)",
                                style = MaterialTheme.appTypography.subtitleSemiBold16
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        BaseOutlineButton(
                            contentAligment = Alignment.CenterStart,
                            text = if (isPromptSelected) uiState.promptInput
                            else "원하는 프롬프트를 선택해주세요",
                            showTrailingArrow = true,
                            textStyle = MaterialTheme.appTypography.bodyMedium16_h22.copy(
                                color = if (isPromptSelected) MaterialTheme.extendedColors.textPointBlue
                                else MaterialTheme.extendedColors.textGhost
                            ),
                            color = if (isPromptSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onOpenPromptSheet,
                        )

                    }


                    // 🔹 바텀시트
                    if (uiState.showBottomSheet) {
                        BottomSheetContainerRightTopConfirm(
                            onDismiss = onCloseSheet,
                            onConfirm = onConfirmPrompt,
                            titleText = setSheetTitle,
                            titleTextStyle = MaterialTheme.appTypography.subtitleSemiBold18,
                            lockDrag = true,
                            heightFraction = 0.6f,
                            hasScrollbar = true,
                            content = {
                                RequestPromptOptionList(
                                    modifier = Modifier.fillMaxSize(),
                                    options = uiState.promptOptions,
                                    selectedId = uiState.selectedPromptId,
                                    onSelect = viewModel::onPromptSelected,
                                    scrollbarThumbColor = MaterialTheme.extendedColors.viewBackgroundGray500,
                                    scrollbarTrackColor = MaterialTheme.extendedColors.viewBackgroundGray200,
                                )
                            },
                            onCompleteEnable = true,
                        )
                        /*
                        BottomSheetContainer(
                            titleText = "난이도를 선택해주세요",
                            onDismiss = {
                                viewModel.closeBottomSheet()
                            }
                        ) {

                        }*/
                    }
                }
            )

        },
    )
}
