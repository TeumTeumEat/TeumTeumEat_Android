package com.teumteumeat.teumteumeat.ui.component.quiz.multi_choice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun QuizMultiChoiceCard(
    modifier: Modifier = Modifier,
    questionIndex: Int,
    question: String,
    options: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
) {

    val theme = MaterialTheme.extendedColors
    val btnContainerColor = if (selectedIndex != null) theme.primaryContainer
        else theme.unselectedContainer

    val btnTextColor = if (selectedIndex != null) theme.primary
        else theme.textOnUnselected

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
            color = MaterialTheme.extendedColors.backgroundW100
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

                // 🔹 하단 라디오 그룹 선택 버튼
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    options.forEachIndexed { index, option ->
                        QuizOptionButton(
                            text = option,
                            isSelected = selectedIndex == index,
                            onClick = {
                                onSelect(index)
                            }
                        )
                    }
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
fun QuizMultiChoiceCardPreview() {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {


            QuizMultiChoiceCard(
                questionIndex = 1,
                question = "이거는 저거일까?",
                options = listOf("이거다", "저거다", "둘 다 아니다"),
                selectedIndex = selectedIndex,
                onSelect = { index ->
                    selectedIndex = index
                }
            )

            QuizMultiChoiceCard(
                questionIndex = 2,
                question = "이거는 저거일까?",
                options = listOf("이거다", "저거다", "둘 다 아니다"),
                selectedIndex = selectedIndex,
                onSelect = { index ->
                    selectedIndex = index
                }
            )

        }
    }
}