package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
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
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun GuideScreen(
    isChecked: Boolean,
    onBackClick: () -> Unit,
    onQuizClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
) {

    val theme = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography

    /* ================= 로티파일 리소스 (홈 화면과 동일한 냠냠지식 캐릭터) ================= */
    val backComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.home_eat_before)
    )
    val progress by animateLottieCompositionAsState(
        composition = backComposition,
        iterations = LottieConstants.IterateForever,
    )

    BackHandler {
        onBackClick()
    }

    DefaultMonoBg() {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.backSurface),
            content = { padding ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // 홈 화면과 동일한 로티 컴포지션: w=360, h=572 (home_eat_before.json)
                    // card 레이어: rect=[303.234, 442.253], transform scale=[98.257%, 98.837%]
                    // card 중심 (컴포지션 좌표): x=180(수평 중앙), y=286+21.512=307.512
                    val lottieRenderScale = minOf(
                        maxWidth.value / 360f,
                        maxHeight.value / 572f
                    )
                    val cardRenderedW = (303.234f * 0.98257f * lottieRenderScale).dp
                    val cardRenderedH = (442.253f * 0.98837f * lottieRenderScale).dp
                    val cardOffsetY = (21.512f * lottieRenderScale).dp

                    // 홈 화면과 동일한 냠냠지식 캐릭터 로티 애니메이션을 배경으로 사용
                    LottieAnimation(
                        composition = backComposition,
                        progress = { progress },
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {
                        /**
                         * 타이틀 바
                         */
                        TitleBar(
                            title = "오늘의 냠냠지식",
                            onBackClick = { onBackClick() }
                        )
                    }

                    // 로티 애니메이션의 카드 사각형 영역에 맞춰 기존 컨텐츠를 정렬
                    Box(
                        modifier = Modifier
                            .width(cardRenderedW)
                            .height(cardRenderedH)
                            .align(Alignment.Center)
                            .offset(y = cardOffsetY),
                        contentAlignment = Alignment.Center
                    ) {
                        QuizGuideCard(
                            isDontShowChecked = isChecked,
                            onCheckedChange = onCheckedChange,
                            onQuizClick = onQuizClick
                        )
                    }
                }
            },
        )
    }
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
    // 카드 배경은 로티 애니메이션의 card 레이어를 그대로 사용한다.
    // 컨테이너를 투명하게 두어야 로티에서 카드 위로 겹쳐 그려지는 캐릭터 연출이 가려지지 않는다.
    Card(
        modifier = modifier
            .fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.extendedColors.textPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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

            Column(
                modifier = Modifier
                    .wrapContentWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                DontShowAgainCheckbox(
                    checked = isDontShowChecked,
                    onCheckedChange = onCheckedChange
                )

                Spacer(modifier = Modifier.height(8.dp))

                BaseFillButton(
                    modifier = Modifier
                        .wrapContentWidth()
                        .width(260.dp),
                    onClick = onQuizClick,
                    text = "퀴즈 풀러가기",
                )
            }
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
        modifier = Modifier.wrapContentWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
        modifier = Modifier
            .wrapContentWidth()
            .width(260.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange(!checked) },
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



