package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.theme.Typography


@Composable
fun OnBoardingFirstScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext : () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    // 🐧 캐릭터 Lottie 애니메이션
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.onboarding_comp)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    DefaultMonoBg(
        color = MaterialTheme.colorScheme.surface,
        extensionHeight = 0.dp,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    SpeechBubble(
                        text = "틈틈잇에 오신 걸 환영해요!\n" +
                                "저는 틈틈이예요",
                    )

                    Spacer(modifier = Modifier.height(11.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center

                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                        )
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
                        text = "시작하기",
                        textStyle = Typography.labelMedium.copy(
                            lineHeight = 24.sp
                        ),
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

/*
@Preview(showBackground = true)
@Composable
fun OnBoardingPreview() {

    val fakeViewModel = remember { OnBoardingViewModel() }
    TeumTeumEatTheme {
        OnBoardingFirstScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateOnBoardingMain(errorMessage = "한글 또는 영문만 입력할 수 있어요", isNameValid = false),
            onNext = {}
        )
    }
}*/
