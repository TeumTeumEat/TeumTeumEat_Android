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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.utils.appTypography

@Composable
fun QuizFinishScreen(
    modifier: Modifier = Modifier,
    correctCount: Int,
    onCloseClick: () -> Unit,
    onNextClick: () -> Unit,
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
                .clickable { onCloseClick() },
            tint = Color.Black
        )

        // 메인 컨텐츠
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // 캐릭터 이미지
            Image(
                painter = painterResource(id = R.drawable.char_exited_quiz_finish),
                contentDescription = "",
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

/** 🔍 Preview */
@Preview(showBackground = true)
@Composable
private fun QuizFinishScreenPreview() {
    MaterialTheme {
        QuizFinishScreen(
            correctCount = 2,
            onCloseClick = {},
            modifier = Modifier.padding(0.dp),
            onNextClick = {

            }
        )
    }
}

