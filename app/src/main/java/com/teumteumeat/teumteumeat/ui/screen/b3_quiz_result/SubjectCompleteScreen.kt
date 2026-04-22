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
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun SubjectCompleteScreen(
    modifier: Modifier = Modifier,
    onGoHomeClick: () -> Unit,
    onStartNewSubjectClick: () -> Unit,
) {
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
                .clickable { onGoHomeClick() },
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
                text = "해당 주제를 \n모두 완주했어요!",
                style = MaterialTheme.appTypography.titleSemiBold32.copy(
                    textAlign = TextAlign.Center
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(23.dp))
            
            // 서브 타이틀
            Text(
                text = "새로운 여정을 시작해 볼까요?",
                style = MaterialTheme.appTypography.subtitleSemiBold20.copy(
                    textAlign = TextAlign.Center
                ),
                color = Color.Black
            )

        }

        /** 🔹 하단 버튼 영역 */
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "홈으로 가기",
                style = MaterialTheme.appTypography.subtitleSemiBold16.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.extendedColors.primary,
                ),
                modifier = Modifier
                    .clickable(
                        onClick = onGoHomeClick
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 20.dp),
            ) {
                BaseFillButton(
                    modifier = Modifier
                        .weight(1f),
                    onClick = onStartNewSubjectClick,
                    text = "새로운 틈틈잇 시작하기"
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview(){
    TeumTeumEatTheme {
        SubjectCompleteScreen(
            onGoHomeClick = {  },
            onStartNewSubjectClick = {  },
        )
    }
}


