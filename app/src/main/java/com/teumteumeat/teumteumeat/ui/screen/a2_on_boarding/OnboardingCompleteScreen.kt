package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun OnBoardingSuccessScreen(
    nickname: String,
    onStartClick: () -> Unit,
) {
    BackHandler(enabled = true) { /* 온보딩 완료 후 뒤로가기 차단 */ }

    // 🎬 Lottie Composition 로드
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.onboarding_comp)
    )

    // 🎬 애니메이션 상태
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.systemBars) // ✅ SafeArea,
    ) {

        // 🔹 상단 ~ 중앙 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            // 🗨️ 말풍선 이미지
            SpeechBubble(
                text = "모든 준비 끝!\n매일 배달될 틈틈잇을 확인해 보세요.",
                textStyle = MaterialTheme.appTypography.subtitleSemiBold20.copy(
                    lineHeight = 28.sp
                )
            )

            Spacer(modifier = Modifier.height(11.dp))

            // 🐧 캐릭터 이미지
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(260.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🙌 환영 문구
            Text(
                text = "$nickname 님 환영합니다!",
                style = MaterialTheme.appTypography.btnSemiBold18_h24,
                color = MaterialTheme.extendedColors.textTeritory
            )

            Spacer(Modifier.height(24.dp))

            BaseFillButton(
                text = "시작하기",
                textStyle = MaterialTheme.appTypography.btnBold20_h24.copy(
                    Color.White
                ),
                isEnabled = true,
                onClick = onStartClick,
            )
        }

    }
}


@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_4,
    name = "온보딩 완료 화면",
)
@Composable
private fun OnBoardingSuccessScreenPreview() {
    TeumTeumEatTheme {
        OnBoardingSuccessScreen(
            nickname = "틈틈잇",
            onStartClick = {}
        )
    }
}