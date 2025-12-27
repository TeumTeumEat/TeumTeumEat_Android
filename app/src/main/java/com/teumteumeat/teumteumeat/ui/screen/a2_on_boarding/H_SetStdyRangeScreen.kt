package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.teumteumeat.teumteumeat.ui.component.WeekRadioGroup
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun SetStudyRangeScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnBoardingMain,
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
                        .padding(),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.Companion.height(60.dp))
                    Text(
                        "공부하고자 하는 기간을 선택하세요!",
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
                    Spacer(modifier = Modifier.Companion.height(25.dp))

                    WeekRadioGroup(
                        options = studyWeekOptions,
                        selectedValue = uiState.selectedStudyWeek,
                        onSelect = { option ->
                            viewModel.onStudyWeekSelected(option.value)
                        }
                    )
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
                        isEnabled = uiState.selectedStudyWeek != null,
                        onClick = {
                            // todo. 뷰모델 함수로 서버로 학습 목표 생성 요청하기
                            viewModel.onCreateGoalClick()
                            onNext()
                        },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}