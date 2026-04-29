package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.component.WeekRadioGroup
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

private val studyWeekOptions = listOf(
    StudyWeekOption("1주", 1),
    StudyWeekOption("2주", 2),
    StudyWeekOption("3주", 3),
    StudyWeekOption("4주", 4),
)

@Composable
fun SetStudyPeriodScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    SetStudyPeriodScreenContent(
        studyPeriod = uiState.studyPeriod,
        onStudyWeekSelected = { viewModel.onStudyWeekSelected(it) },
        onNext = onNext,
    )
}

@Composable
fun SetStudyPeriodScreenContent(
    studyPeriod: Int?,
    onStudyWeekSelected: (Int) -> Unit,
    onNext: () -> Unit,
) {
    DefaultMonoBg(
        extensionHeight = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpeechBubble(text = "얼마 동안 공부해 볼까요?\n내 속도에 맞는 학습 기간을 정해보세요")
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_five_four),
                        contentDescription = "앞을 보는 케릭터",
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(15.dp))

                    WeekRadioGroup(
                        options = studyWeekOptions,
                        selectedValue = studyPeriod,
                        onSelect = { option -> onStudyWeekSelected(option.value) }
                    )

                    Spacer(Modifier.height(60.dp))
                }

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
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = MaterialTheme.appTypography.btnBold20_h24.copy(
                            color = MaterialTheme.extendedColors.backgroundW100
                        ),
                        isEnabled = studyPeriod != null,
                        onClick = {
                            onNext()
                        },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}

@Preview(showBackground = true, name = "SetStudyPeriod – 미선택", group = "Onboarding")
@Composable
private fun SetStudyPeriodScreenPreviewEmpty() {
    TeumTeumEatTheme {
        SetStudyPeriodScreenContent(
            studyPeriod = null,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "SetStudyPeriod – 2주 선택")
@Composable
private fun SetStudyPeriodScreenPreviewSelected() {
    TeumTeumEatTheme {
        SetStudyPeriodScreenContent(
            studyPeriod = 1,
            onStudyWeekSelected = {},
            onNext = {},
        )
    }
}