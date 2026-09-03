package com.teumteumeat.teumteumeat.ui.component.modal.bubble

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Preview(showBackground = true, backgroundColor = 0xFFF3F4F6)
@Composable
fun PreviewHomeSpeechBubble() {
    var isQuizCompleted by remember { mutableStateOf(false) }

    val bubbleScale by animateFloatAsState(
        targetValue = if (isQuizCompleted) 1.0f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "bubble_grow_animation"
    )

    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {

                // 1. 퀴즈 완료 카드
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clickable { isQuizCompleted = true },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "🎉 오늘의 퀴즈 카드 (Click!)",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray
                        )
                    }
                }

                // 2. 카드 아래쪽을 살짝 덮으면서 나타나는 말풍선
                if (bubbleScale > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-16).dp, y = 30.dp)
                            .graphicsLayer {
                                scaleX = bubbleScale
                                scaleY = bubbleScale
                                transformOrigin = TransformOrigin(0.8f, 0f)
                                alpha = if (bubbleScale > 0.3f) 1f else 0f
                            }
                    ) {
                        HomeSpeechBubble(
                            text = "음냐냐.. 퀴즈 더 풀고 싶다아.. Click!",
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

/**
 * 홈 화면 카드 위에 표시되는 말풍선.
 * 최대 2줄까지 표시하며, 2줄 안에 다 들어가지 않으면 글자 크기를 자동으로 줄인다.
 * 폰트 크기는 기존 14sp를 유지하되, 2줄 표시에 맞는 lineHeight(20sp)를 적용한다.
 */
@Composable
fun HomeSpeechBubble(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = MaterialTheme.extendedColors.backgroundW100,
    shadowElevation: Dp = 6.dp,
    cornerRadius: Dp = 16.dp,
    tailWidth: Dp = 19.dp,
    tailHeight: Dp = 14.dp,
    tailPaddingEnd: Dp = 32.dp,
    onClick: () -> Unit
) {
    val theme = MaterialTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }

    // 위에서 만든 커스텀 도형 객체 생성
    val bubbleShape = SpeechBubbleShape(cornerRadius, tailWidth, tailHeight, tailPaddingEnd)

    val baseStyle = MaterialTheme.appTypography.bodyMedium14_20.copy(color = theme.textPrimary)
    var textStyle by remember(text) { mutableStateOf(baseStyle) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 리플(클릭) 이펙트 제거
                onClick = {
                    onClick()
                }
            )
            // 🌟 말풍선 도형(꼬리 포함) 모양 그대로 드롭 섀도우를 그립니다.
            .shadow(elevation = shadowElevation, shape = bubbleShape, clip = false)
            // 같은 도형으로 배경색 칠하기
            .background(color = backgroundColor, shape = bubbleShape)
            // 내용물이 꼬리 영역을 침범하지 않도록 상단에 패딩 추가
            .padding(top = tailHeight)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = textStyle,
            textAlign = TextAlign.Center,
            maxLines = BUBBLE_MAX_LINES,
            softWrap = true,
            modifier = Modifier.drawWithContent { if (readyToDraw) drawContent() },
            onTextLayout = { result ->
                if (result.hasVisualOverflow && textStyle.fontSize > MIN_BUBBLE_FONT_SIZE) {
                    textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.95f)
                } else {
                    readyToDraw = true
                }
            }
        )
    }
}

private const val BUBBLE_MAX_LINES = 2
private val MIN_BUBBLE_FONT_SIZE = 10.sp
