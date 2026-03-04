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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.MinuteRadioGroup
import com.teumteumeat.teumteumeat.ui.component.SpeechBubble
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.ui.theme.Typography

@Composable
fun OnBoardingSetUsingApptimeScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    val isSetAllTimeValid = uiState.isSetWorkInTime && uiState.isSetWorkOutTime


    DefaultMonoBg(
        extensionHeight = 0.dp,
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
                    SpeechBubble(text = "틈틈잇 하루 목표 시간을\n" +
                            "알려주세요!")
                    Spacer(modifier = Modifier.Companion.height(20.dp))
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_three),
                        contentDescription = "앞을 보는 케릭터",
                        modifier = Modifier.Companion.size(width = 200.dp, height = 162.dp),
                        contentScale = ContentScale.Companion.Fit,
                    )
                    Spacer(modifier = Modifier.Companion.height(25.dp))

                    MinuteRadioGroup(
                        options = listOf(5, 7, 10, 15),
                        selectedMinute = uiState.selectedMinute,
                        onSelect = { viewModel.onMinuteSelected(it) }
                    )

                    Spacer(Modifier.height(100.dp))

                }

                // 2️⃣ 하단 그라데이션 (페이드 효과)
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
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = Typography.labelMedium.copy(
                            lineHeight = 24.sp
                        ),
                        isEnabled = uiState.selectedMinute != null,
                        onClick = {
                            // viewModel.saveCommuteInfo()
                            onNext()
                        },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}


@Preview(showBackground = true)
@Composable
fun OnBoardingSetUsingApptimeScreen() {

    val fakeViewModel : OnBoardingViewModel = hiltViewModel()
    TeumTeumEatTheme {
        /*OnBoardingSetUsingApptimeScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateOnBoardingMain(errorMessage = "한글 또는 영문만 입력할 수 있어요", isNameValid = false),
            onNext = {},
            onPrev = {}
        )*/
    }
}