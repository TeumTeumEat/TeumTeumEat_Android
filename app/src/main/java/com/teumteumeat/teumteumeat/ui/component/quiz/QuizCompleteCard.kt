package com.teumteumeat.teumteumeat.ui.component.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun QuizCompleteCard(
    modifier: Modifier = Modifier,
    title: String = "모든 퀴즈를 풀었어요!",
    buttonText: String = "채점하러 가기",
    onButtonClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 24.dp,
                    bottom = 32.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 🔹 상단 이미지 영역 (추후 이미지/일러스트 대체 가능)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        color = Color(0xFFF3F3F3),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            // 🔹 완료 메시지
            Text(
                text = title,
                style = MaterialTheme.appTypography.titleBold20,
                color = Color.Black
            )

            // 🔹 CTA 버튼
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.extendedColors.primaryContainer,
                onClick = onButtonClick
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.appTypography.btnSemiBold20_h24,
                        color = MaterialTheme.extendedColors.primary
                    )
                }
            }
        }
    }
}

@Preview(
    name = "QuizCompleteCard - Gallery",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
fun QuizCompleteCardGalleryPreview() {
    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // 기본 상태
            QuizCompleteCard(
                title = "모든 퀴즈를 풀었어요!",
                buttonText = "채점하러 가기",
                onButtonClick = {}
            )

            // 텍스트 길이 변화 테스트
            QuizCompleteCard(
                title = "모든 퀴즈를 완료했어요 🎉\n이제 결과를 확인해볼까요?",
                buttonText = "결과 확인하기",
                onButtonClick = {}
            )
        }
    }
}

