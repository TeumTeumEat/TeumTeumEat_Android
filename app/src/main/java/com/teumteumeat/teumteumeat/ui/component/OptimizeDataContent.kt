package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.domain.model.RequestPromptOption
import com.teumteumeat.teumteumeat.domain.model.defaultRequestPromptOptions
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.component.radio_group.DifficultyRadioGroup
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.DifficultyOption
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

/**
 * 온보딩/목표 추가 플로우에서 공용으로 사용하는 퀴즈 최적화 설정 화면.
 *
 * @param speechBubbleText 상단 캐릭터 말풍선에 표시할 텍스트
 * @param difficulty 현재 선택된 난이도
 * @param difficultyOptions 난이도 선택지 목록
 * @param promptInput 현재 확정된 퀴즈 유형 프롬프트 텍스트 (미선택이면 빈 문자열)
 * @param promptOptions 바텀시트에 표시할 프롬프트 선택지 목록
 * @param selectedPromptId 바텀시트에서 임시 선택 중인 항목 id
 * @param showBottomSheet 바텀시트 표시 여부
 * @param sheetTitle 바텀시트 상단 타이틀
 * @param isNextEnabled 다음 버튼 활성화 여부
 * @param onDifficultySelected 난이도 선택 콜백
 * @param onOpenPromptSheet 퀴즈 유형 버튼 클릭 → 시트 열기
 * @param onCloseSheet 바텀시트 닫기 콜백
 * @param onConfirmPrompt 바텀시트 확인 버튼 콜백
 * @param onPromptSelected 바텀시트 항목 선택 콜백
 * @param onNext 다음으로 버튼 클릭 콜백
 */
@Composable
fun OptimizeDataContent(
    speechBubbleText: String,
    difficulty: Difficulty,
    difficultyOptions: List<DifficultyOption>,
    promptInput: String,
    promptOptions: List<RequestPromptOption>,
    selectedPromptId: String?,
    showBottomSheet: Boolean,
    sheetTitle: String,
    isNextEnabled: Boolean,
    onDifficultySelected: (Difficulty) -> Unit,
    onOpenPromptSheet: () -> Unit,
    onCloseSheet: () -> Unit,
    onConfirmPrompt: () -> Unit,
    onPromptSelected: (RequestPromptOption) -> Unit,
    onNext: () -> Unit,
) {
    val isPromptSelected = promptInput.isNotBlank()
    val scrollState = rememberScrollState()

    DefaultMonoBg(
        extensionHeight = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        content = {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .focusable(),
                containerColor = Color.Transparent,
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.ime)
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp)
                            .padding(horizontal = 20.dp),
                    ) {
                        BaseFillButton(
                            text = "다음으로",
                            textStyle = MaterialTheme.appTypography.btnBold20_h24.copy(
                                color = MaterialTheme.extendedColors.backgroundW100
                            ),
                            isEnabled = isNextEnabled,
                            onClick = onNext,
                            conerRadius = 16.dp
                        )
                    }
                },
                content = { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp)
                            .verticalScroll(scrollState)
                            .padding(bottom = 120.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))
                        SpeechBubble(text = speechBubbleText)
                        Image(
                            painter = painterResource(R.drawable.char_onboarding_five_three),
                            contentDescription = "앞을 보는 캐릭터",
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(modifier = Modifier.height(15.dp))
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
                            selected = difficulty,
                            onSelect = onDifficultySelected,
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                "퀴즈 유형 지정 (선택)",
                                style = MaterialTheme.appTypography.subtitleSemiBold16
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        BaseOutlineButton(
                            contentAligment = Alignment.CenterStart,
                            text = if (isPromptSelected) promptInput
                            else "나만의 퀴즈/학습 스타일을 선택하세요",
                            showTrailingIcon = true,
                            textStyle = MaterialTheme.appTypography.bodyMedium16_h22.copy(
                                color = if (isPromptSelected) MaterialTheme.extendedColors.textPointBlue
                                else MaterialTheme.extendedColors.textGhost
                            ),
                            color = if (isPromptSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onOpenPromptSheet,
                        )
                    }

                    if (showBottomSheet) {
                        BottomSheetContainerRightTopConfirm(
                            onDismiss = onCloseSheet,
                            onConfirm = onConfirmPrompt,
                            titleText = sheetTitle,
                            titleTextStyle = MaterialTheme.appTypography.subtitleSemiBold18,
                            lockDrag = true,
                            heightFraction = 0.6f,
                            hasScrollbar = true,
                            content = {
                                RequestPromptOptionList(
                                    modifier = Modifier.fillMaxSize(),
                                    options = promptOptions,
                                    selectedId = selectedPromptId,
                                    onSelect = onPromptSelected,
                                    scrollbarThumbColor = MaterialTheme.extendedColors.viewBackgroundGray500,
                                    scrollbarTrackColor = MaterialTheme.extendedColors.viewBackgroundGray200,
                                )
                            },
                            onCompleteEnable = true,
                        )
                    }
                }
            )
        },
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

private val previewDifficultyOptions = listOf(
    DifficultyOption("상", Difficulty.HARD),
    DifficultyOption("중", Difficulty.MEDIUM),
    DifficultyOption("하", Difficulty.EASY),
)

@Preview(name = "난이도 미선택 (초기)", showBackground = true)
@Composable
private fun OptimizeDataContent_InitialPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.NONE,
            difficultyOptions = previewDifficultyOptions,
            promptInput = "",
            promptOptions = defaultRequestPromptOptions,
            selectedPromptId = null,
            showBottomSheet = false,
            sheetTitle = "요청 프롬프트 선택",
            isNextEnabled = false,
            onDifficultySelected = {},
            onOpenPromptSheet = {},
            onCloseSheet = {},
            onConfirmPrompt = {},
            onPromptSelected = {},
            onNext = {},
        )
    }
}

@Preview(name = "난이도 선택 완료 (다음 활성)", showBackground = true)
@Composable
private fun OptimizeDataContent_DifficultySelectedPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.MEDIUM,
            difficultyOptions = previewDifficultyOptions,
            promptInput = "",
            promptOptions = defaultRequestPromptOptions,
            selectedPromptId = null,
            showBottomSheet = false,
            sheetTitle = "요청 프롬프트 선택",
            isNextEnabled = true,
            onDifficultySelected = {},
            onOpenPromptSheet = {},
            onCloseSheet = {},
            onConfirmPrompt = {},
            onPromptSelected = {},
            onNext = {},
        )
    }
}

@Preview(name = "퀴즈 유형 선택 완료", showBackground = true)
@Composable
private fun OptimizeDataContent_PromptSelectedPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.HARD,
            difficultyOptions = previewDifficultyOptions,
            promptInput = "출퇴근길에 가볍게 풀 수 있게 만들어주세요.",
            promptOptions = defaultRequestPromptOptions,
            selectedPromptId = "commute",
            showBottomSheet = false,
            sheetTitle = "요청 프롬프트 선택",
            isNextEnabled = true,
            onDifficultySelected = {},
            onOpenPromptSheet = {},
            onCloseSheet = {},
            onConfirmPrompt = {},
            onPromptSelected = {},
            onNext = {},
        )
    }
}

@Preview(name = "바텀시트 표시 상태", showBackground = true)
@Composable
private fun OptimizeDataContent_BottomSheetPreview() {
    TeumTeumEatTheme {
        OptimizeDataContent(
            speechBubbleText = "원하는 난이도를 선택해주세요\n추가로 원하는 내용이 있다면 알려주세요!",
            difficulty = Difficulty.MEDIUM,
            difficultyOptions = previewDifficultyOptions,
            promptInput = "",
            promptOptions = defaultRequestPromptOptions,
            selectedPromptId = "trend",
            showBottomSheet = true,
            sheetTitle = "요청 프롬프트 선택",
            isNextEnabled = true,
            onDifficultySelected = {},
            onOpenPromptSheet = {},
            onCloseSheet = {},
            onConfirmPrompt = {},
            onPromptSelected = {},
            onNext = {},
        )
    }
}