package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import com.teumteumeat.teumteumeat.ui.component.canvas_icon.NumberBadge
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.teumteumeat.teumteumeat.ui.component.CheckBoxCircle
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.theme.btnGray200

@Composable
fun GuideScreen(
    isChecked: Boolean,
    onBackClick: () -> Unit,
    onQuizClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {

    val theme = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography

    BackHandler {
        onBackClick()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backSurface),
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        /**
                         * 타이틀 바
                         */
                        TitleBar(
                            title = "오늘의 냠냠지식",
                            onBackClick = { onBackClick() }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier,
                            ) {
                                /*Image(
                                    painterResource(id = R.drawable.back_quiz_guide),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .offset(
                                            x = (30).dp,     // 👉 왼쪽으로 +30dp
                                        ),
                                    contentScale = ContentScale.Fit
                                )*/


                                Column(
                                    modifier = Modifier
                                        .padding(
                                            horizontal = 30.dp
                                        )
                                        .padding(top = 114.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    QuizGuideCard(
                                        isDontShowChecked = isChecked,
                                        onCheckedChange = onCheckedChange,
                                        onQuizClick = onQuizClick
                                    )
                                }

                            }


                        }

                    }
                }
            }
        },
    )
}

@Preview(
    name = "GuideScreen - Light",
    showBackground = true,
    device = Devices.PIXEL_4
)
@Composable
fun GuideScreenPreview() {
    TeumTeumEatTheme { // 🔥 실제 앱 테마로 감싸기
        GuideScreen(
            onBackClick = {},
            onQuizClick = {},
            onCheckedChange = {},
            isChecked = false,
        )
    }
}

@Composable
fun QuizGuideCard(
    modifier: Modifier = Modifier,
    isDontShowChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onQuizClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 2.dp,
            color = btnGray200
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.extendedColors.backgroundW100,
            contentColor = MaterialTheme.extendedColors.textPrimary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            QuizTypeChip()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "오늘의 지식과 관련한\n퀴즈 맞춰봐요!",
                style = MaterialTheme.appTypography.titleBold24.copy(
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            QuizGuideBulletList(3)

            Spacer(modifier = Modifier.height(40.dp))

            DontShowAgainCheckbox(
                checked = isDontShowChecked,
                onCheckedChange = onCheckedChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            BaseFillButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = onQuizClick,
                text = "퀴즈 풀러가기",
                btnHeight = 50
            )
        }
    }
}

@Composable
private fun QuizTypeChip() {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.extendedColors.primary
    ) {
        Text(
            text = "O/X 퀴즈",
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.appTypography.bodySemiBold18,
            color = MaterialTheme.extendedColors.textOnPrimary
        )
    }
}

@Composable
private fun QuizGuideBulletList(numberCount : Int) {
    val guideTexts = listOf(
        "O/X와 객관식 문제가 랜덤으로 나와요",
        "문제가 끝나면 정답 확인이 가능해요",
        "지난 문제는 히스토리에서 확인해요"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        guideTexts
            .take(numberCount)
            .forEachIndexed { index, text ->
                QuizGuideBulletItem(
                    index = index + 1,   // ✅ 1부터 시작
                    text = text,
                    highlightWord = if(index == guideTexts.lastIndex) "히스토리" else ""
                )
            }
    }
}



@Composable
private fun QuizGuideBulletItem(
    text: String,
    index: Int,
    highlightWord: String = "",
    highlightColor: Color = MaterialTheme.extendedColors.primary
) {
    val annotatedText = buildAnnotatedString {
        val startIndex = text.indexOf(highlightWord)

        if (startIndex >= 0) {
            val endIndex = startIndex + highlightWord.length

            append(text.substring(0, startIndex))

            pushStyle(
                SpanStyle(color = highlightColor)
            )
            append(highlightWord)
            pop()

            append(text.substring(endIndex))
        } else {
            append(text)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberBadge(number = index)

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = annotatedText,
            style = MaterialTheme.appTypography.bodyMedium14_20.copy(
                textAlign = TextAlign.Start
            )
        )
    }
}


@Composable
private fun DontShowAgainCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        CheckBoxCircle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "이 안내 다시 보지 않기",
            style = MaterialTheme.appTypography.captionRegular14,
            color = Color.Gray
        )
    }
}



