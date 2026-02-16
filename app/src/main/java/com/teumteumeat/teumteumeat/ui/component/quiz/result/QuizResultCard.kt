package com.teumteumeat.teumteumeat.ui.component.quiz.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

enum class QuizResultType {
    CORRECT,
    WRONG
}

@Composable
fun QuizResultCard(
    modifier: Modifier = Modifier,
    questionIndex: Int,
    title: String,
    answer: String,
    explanation: String,
    resultType: QuizResultType,
) {
    val theme = MaterialTheme.extendedColors

    // 🔹 상태에 따른 컬러 분기
    val accentColor = when (resultType) {
        QuizResultType.CORRECT -> theme.primary
        QuizResultType.WRONG -> theme.error
    }

    val badgeBackground = when (resultType) {
        QuizResultType.CORRECT -> theme.primaryContainer
        QuizResultType.WRONG -> theme.errorContainer
    }

    val badgeText = when (resultType) {
        QuizResultType.CORRECT -> "정답"
        QuizResultType.WRONG -> "오답"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🔹 상단 영역 (Q1 + 정답/오답 뱃지)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q$questionIndex",
                    style = MaterialTheme.appTypography.titleBold24,
                    color = accentColor
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = badgeBackground
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        text = badgeText,
                        style = MaterialTheme.appTypography.captionSemiBold16,
                        color = accentColor
                    )
                }
            }

            // 🔹 질문 제목
            Text(
                text = title,
                style = MaterialTheme.appTypography.titleBold24,
                color = Color.Black
            )

            // 🔹 정답 표시
            Text(
                text = "정답 : $answer",
                style = MaterialTheme.appTypography.bodySemiBold18,
                color = accentColor
            )

            // 🔹 해설
            Text(
                text = explanation,
                style = MaterialTheme.appTypography.bodyMedium14Reg,
                color = Color.Black
            )
        }
    }
}

@Preview(
    name = "QuizResultCard - Gallery",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
fun QuizResultCardGalleryPreview() {
    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            QuizResultCard(
                questionIndex = 1,
                title = "Title_Question",
                answer = "answer",
                explanation = "Body_explanation",
                resultType = QuizResultType.CORRECT
            )

            QuizResultCard(
                questionIndex = 1,
                title = "Title_Question",
                answer = "answer",
                explanation = "Body_explanation",
                resultType = QuizResultType.WRONG
            )
        }
    }
}
