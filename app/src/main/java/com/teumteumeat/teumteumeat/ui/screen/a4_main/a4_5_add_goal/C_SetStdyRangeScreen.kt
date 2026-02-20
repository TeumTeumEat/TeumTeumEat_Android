package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.SpeechBubble
import com.teumteumeat.teumteumeat.ui.component.WeekRadioGroup
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingViewModel
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.StudyWeekOption
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.UiStateOnboardingState
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun AddGoalSetStudyRangeScreen(
    name: String,
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    val studyWeekOptions = listOf(
        StudyWeekOption("1주", 1),
        StudyWeekOption("2주", 2),
        StudyWeekOption("3주", 3),
        StudyWeekOption("4주", 4),
    )

    DefaultMonoBg(
        color = MaterialTheme.colorScheme.surface,
        content = {
            Box(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .padding()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.Companion.height(4.dp))
                    SpeechBubble(text = "선택한 주제에 대해\n" +
                            "공부할 기간을 선택해주세요!")
                    Spacer(modifier = Modifier.Companion.height(12.dp))
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_five_four),
                        contentDescription = "앞을 보는 케릭터",
                        contentScale = ContentScale.Companion.Fit,
                    )
                    Spacer(modifier = Modifier.Companion.height(20.dp))

                    WeekRadioGroup(
                        options = studyWeekOptions,
                        selectedValue = uiState.studyPeriod,
                        onSelect = { option ->
                            viewModel.onStudyWeekSelected(option.value)
                        }
                    )

                    Spacer(Modifier.height(150.dp))

                }

                Column(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = MaterialTheme.appTypography.btnBold20_h24.copy(
                            color = MaterialTheme.extendedColors.backgroundW100
                        ),
                        isEnabled = uiState.studyPeriod != null,
                        onClick = { onNext() },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}