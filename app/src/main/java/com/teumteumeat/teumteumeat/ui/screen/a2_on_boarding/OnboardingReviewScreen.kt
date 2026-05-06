package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.data.mapper.toLable
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.ui.component.ReviewContent
import com.teumteumeat.teumteumeat.ui.component.ReviewInfoItem
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun ReviewScreen(
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val subjectLabel = if (uiState.goalTypeUiState == GoalTypeUiState.CATEGORY) "관심분야" else "문서이름"
    val subjectText = when (uiState.goalTypeUiState) {
        GoalTypeUiState.DOCUMENT -> uiState.selectedFileName.ifEmpty { "선택된 파일 없음" }
        GoalTypeUiState.CATEGORY -> listOfNotNull(
            uiState.categorySelection.depth1?.name,
            uiState.categorySelection.depth2?.name,
            uiState.categorySelection.depth3?.name,
            uiState.categorySelection.depth4?.name,
        ).joinToString(" > ").ifEmpty { "선택된 카테고리 없음" }

        else -> "선택 안함"
    }
    val promptText = if (uiState.promptInput.isBlank()) "미선택" else uiState.promptInput
    val studyPeriodText = uiState.studyPeriod?.let { "${it}주" } ?: "기간 설정 안함"

    ReviewContent(
        speechBubbleText = "모든 설정이 끝났어요.\n지금 바로 시작해 볼까요?",
        hintText = "입력한 정보는 마이페이지에서 수정할 수 있어요",
        subjectLabel = subjectLabel,
        subjectText = subjectText,
        difficultyText = uiState.difficulty.toLable(),
        promptText = promptText,
        studyPeriodText = studyPeriodText,
        onNext = { viewModel.submitOnBoarding() },
        leadingInfoContent = {
            ReviewInfoItem(
                label = "학습 분량",
                text = "${uiState.selectedQuestionCnt}문제",
            )
            OnboardingAlarmSection(
                firstAlarmText = uiState.workInTime.toDisplayText(isSelected = true),
                secondAlarmText = uiState.workOutTime.toDisplayText(isSelected = true),
            )
        },
    )
}

@Composable
private fun OnboardingAlarmSection(
    firstAlarmText: String,
    secondAlarmText: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = "알림 시간",
            style = MaterialTheme.appTypography.subtitleSemiBold16,
        )
    }
    BaseOutlineButton(
        text = "1번째 알림",
        textStyle = MaterialTheme.appTypography.captionRegular12.copy(
            color = MaterialTheme.extendedColors.textSecondary
        ),
        subText = firstAlarmText,
        subTextStyle = MaterialTheme.appTypography.btnMedium18_h24.copy(
            color = MaterialTheme.extendedColors.textSecondary
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        isEnabled = false,
    )
    Spacer(modifier = Modifier.height(12.dp))
    BaseOutlineButton(
        text = "2번째 알림",
        textStyle = MaterialTheme.appTypography.captionRegular12.copy(
            color = MaterialTheme.extendedColors.textSecondary
        ),
        subText = secondAlarmText,
        subTextStyle = MaterialTheme.appTypography.btnMedium18_h24.copy(
            color = MaterialTheme.extendedColors.textSecondary
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        isEnabled = false,
    )
    Spacer(modifier = Modifier.height(24.dp))
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "온보딩 - 카테고리 선택 / 프롬프트 선택", group = "Onboarding")
@Composable
private fun ReviewScreen_CategoryPromptPreview() {
    TeumTeumEatTheme {
        ReviewContent(
            speechBubbleText = "모든 설정이 끝났어요.\n지금 바로 시작해 볼까요?",
            hintText = "입력한 정보는 마이페이지에서 수정할 수 있어요",
            subjectLabel = "관심분야",
            subjectText = "IT > 앱개발 > Android",
            difficultyText = "중",
            promptText = "출퇴근길에 가볍게 풀 수 있게 만들어주세요.",
            studyPeriodText = "2주",
            onNext = {},
            leadingInfoContent = {
                ReviewInfoItem(label = "학습 분량", text = "5문제")
                OnboardingAlarmSection(
                    firstAlarmText = "오전 08시 30분",
                    secondAlarmText = "오후 06시 00분",
                )
            },
        )
    }
}

@Preview(showBackground = true, name = "온보딩 - 문서 / 프롬프트 미선택", group = "Onboarding")
@Composable
private fun ReviewScreen_DocumentNoPromptPreview() {
    TeumTeumEatTheme {
        ReviewContent(
            speechBubbleText = "모든 설정이 끝났어요.\n지금 바로 시작해 볼까요?",
            hintText = "입력한 정보는 마이페이지에서 수정할 수 있어요",
            subjectLabel = "문서이름",
            subjectText = "학습자료_2024.pdf",
            difficultyText = "하",
            promptText = "미선택",
            studyPeriodText = "4주",
            onNext = {},
            leadingInfoContent = {
                ReviewInfoItem(label = "학습 분량", text = "5문제")
                OnboardingAlarmSection(
                    firstAlarmText = "오전 07시 00분",
                    secondAlarmText = "오후 07시 00분",
                )
            },
        )
    }
}

@Preview(showBackground = true, name = "온보딩 - 난이도 상 / 4주", group = "Onboarding")
@Composable
private fun ReviewScreen_HardLong_Preview() {
    TeumTeumEatTheme {
        ReviewContent(
            speechBubbleText = "모든 설정이 끝났어요.\n지금 바로 시작해 볼까요?",
            hintText = "입력한 정보는 마이페이지에서 수정할 수 있어요",
            subjectLabel = "관심분야",
            subjectText = "IT > 앱개발 > iOS > Swift",
            difficultyText = Difficulty.HARD.toLable(),
            promptText = "심화 개념까지 깊이 있게 다뤄주세요.",
            studyPeriodText = "4주",
            onNext = {},
            leadingInfoContent = {
                ReviewInfoItem(label = "학습 분량", text = "10문제")
                OnboardingAlarmSection(
                    firstAlarmText = "오전 09시 00분",
                    secondAlarmText = "오후 09시 00분",
                )
            },
        )
    }
}
