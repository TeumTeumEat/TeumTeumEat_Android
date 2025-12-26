package com.teumteumeat.teumteumeat.ui.component.quiz.ox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

enum class CardStatus { Default, Accept, Reject}

@Composable
fun QuizOXCard(
    modifier: Modifier = Modifier,
    questionIndex: Int,
    question: String,
    onYes: () -> Unit,
    isCardStatus: CardStatus = CardStatus.Default,
    onNo: () -> Unit,
) {

    val theme = MaterialTheme.extendedColors
    val containerColor = when(isCardStatus) {
        CardStatus.Accept -> theme.primaryContainer
        CardStatus.Reject -> theme.errorContainer
        CardStatus.Default -> theme.background
    }

    val acceptStyle = resolveQuizButtonStyle(isCardStatus, BtnType.ACCEPT)
    val rejectStyle = resolveQuizButtonStyle(isCardStatus, BtnType.REJECT)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {

        // 🔹 뒤에 깔리는 카드 (그림자 + 스택 효과)
        Box(
            modifier = Modifier
                .offset(y = (-12).dp)
                .fillMaxWidth()
                .height(420.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .background(
                    color = Color.LightGray.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(32.dp)
                )
        )

        // 🔹 실제 카드
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(32.dp),
            color = containerColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // 🔹 상단 질문 영역
                Column {
                    Text(
                        text = "Q$questionIndex",
                        style = MaterialTheme.appTypography.titleBold32,
                        color = MaterialTheme.extendedColors.primary
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = question,
                        style = MaterialTheme.appTypography.titleBold24,
                        color = Color.Black
                    )
                }

                // 🔹 하단 O / X 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuizAnswerButton(
                        modifier = Modifier.weight(1f),
                        background = acceptStyle.background,
                        iconColor = acceptStyle.iconColor,
                        onClick = onYes,
                        isBtnType = BtnType.ACCEPT,
                    )

                    QuizAnswerButton(
                        modifier = Modifier.weight(1f),
                        background = rejectStyle.background,
                        iconColor = rejectStyle.iconColor,
                        onClick = onNo,
                        isBtnType = BtnType.REJECT,
                    )
                }
            }
        }
    }
}

@Preview(
    name = "QuizCard Preview",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
fun QuizCardPreview() {
    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            QuizOXCard(
                questionIndex = 1,
                question = "이거는 저거일까 ?",
                onYes = {},
                onNo = {}
            )

            QuizOXCard(
                questionIndex = 2,
                question = "이 음식은 다이어트에 도움이 될까?",
                onYes = {},
                onNo = {},
                isCardStatus = CardStatus.Accept,
            )

            QuizOXCard(
                questionIndex = 3,
                question = "하루 10분 공부는 의미가 있을까?",
                onYes = {},
                onNo = {},
                isCardStatus = CardStatus.Reject,
            )
        }
    }
}