package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.appTypography

@Composable
fun QuizFinishScreen(
    modifier: Modifier = Modifier,
    correctCount: Int,
    onCloseClick: () -> Unit,
    onNextClick: () -> Unit,
    screenState: UiScreenState,
    onRetryApi: () -> Unit,
) {
    if (screenState is UiScreenState.Error) {
        val errorMessage =
            (screenState as UiScreenState.Error).message

        FullScreenErrorModal(
            errorState = ErrorState(
                title = "문제가 발생했어요",
                description = errorMessage,
                retryLabel = "다시 시도하기",
                onRetry = onRetryApi
            ),
            onBack = onCloseClick,
        )
    }else{
        when (screenState) {

            // ⭐ Idle과 Loading을 묶어서 처리하여 데이터가 없을 때 0이 노출되는 것을 방지합니다.
            UiScreenState.Idle, UiScreenState.Loading -> {
                LoadingScreen(
                    title = "결과 로딩중",
                )
            }

            UiScreenState.Success -> {
                if (correctCount == -1) {
                    LoadingScreen(
                        title = "결과 로딩중",
                        message = "잠시만 기다려주세요.",
                    )
                }else{
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .systemBarsPadding()
                    ) {

                        // ❌ 닫기 버튼 (우측 상단)
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "닫기",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(20.dp)
                                .size(40.dp)
                                .clickable { onCloseClick() },
                            tint = Color.Black
                        )

                        // 메인 컨텐츠
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {

                            // 1. 로티 컴포지션 로드 (raw 폴더의 파일 지정)
                            val composition by rememberLottieComposition(
                                // ⚠️ 주의: 파일명 확장자(.json)는 뺍니다.
                                spec = LottieCompositionSpec.RawRes(R.raw.quiz_comp)
                            )

                            // 2. 애니메이션 상태 제어 (반복 여부 등)
                            val progress by animateLottieCompositionAsState(
                                composition = composition,
                                // 🔥 무한 반복 설정
                                iterations = LottieConstants.IterateForever,
                            )

                            // 3. 화면에 그리기
                            LottieAnimation(
                                composition = composition,
                                progress = { progress }, // 현재 애니메이션 진행 상태
                                // contentScale = ContentScale.Fit, // 필요 시 스케일 설정
                            )


                            Spacer(modifier = Modifier.height(32.dp))

                            // 타이틀
                            Text(
                                text = "${correctCount}문제를 맞췄어요!",
                                style = MaterialTheme.appTypography.titleSemiBold32,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 설명
                            Text(
                                text = "아래 버튼을 눌러\n정답과 해설을 확인해보세요.",
                                style = MaterialTheme.appTypography.bodyMedium16Reg,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                        }

                        // 하단 퀴즈 버튼
                        BaseFillButton(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(vertical = 20.dp, horizontal = 30.dp)
                                .fillMaxWidth(),
                            onClick = onNextClick,
                            text = "결과보기",
                            textStyle = MaterialTheme.appTypography.btnBold20_h24
                        )
                    }
                }
            }

            is UiScreenState.Error -> {}
        }
    }

}

/** 🔍 Preview */
@Preview(showBackground = true)
@Composable
private fun QuizFinishScreenPreview() {
    MaterialTheme {
        QuizFinishScreen(
            modifier = Modifier.padding(0.dp),
            correctCount = 2,
            onCloseClick = {},
            onNextClick = {

            },
            screenState = UiScreenState.Success,
            onRetryApi = {  }
        )
    }
}

