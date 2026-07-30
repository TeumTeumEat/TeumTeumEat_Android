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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
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

// 🎯 일체형 커스텀 도형 (말풍선 본체 + 꼬리)
class SpeechBubbleShape(
    private val cornerRadius: Dp,
    private val tailWidth: Dp,
    private val tailHeight: Dp,
    private val tailPaddingEnd: Dp,
    private val tailTipRadius: Dp = 8.dp // 💡 둥근 정도를 설정하는 새로운 변수 (기본값 4.dp)
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Generic(Path().apply {
            val cr = with(density) { cornerRadius.toPx() }
            val tw = with(density) { tailWidth.toPx() }
            val th = with(density) { tailHeight.toPx() }
            val tpe = with(density) { tailPaddingEnd.toPx() }

            // 꼬리 끝 둥글기가 꼬리 높이보다 커지지 않도록 제한합니다.
            val ttr = with(density) { tailTipRadius.toPx() }.coerceAtMost(th)

            val bodyTop = th // 꼬리 아래부터 본체 시작
            val bodyBottom = size.height
            val bodyLeft = 0f
            val bodyRight = size.width

            val tailTipX = bodyRight - tpe - (tw / 2f)
            val tailLeftX = bodyRight - tpe - tw
            val tailRightX = bodyRight - tpe

            // 💡 둥근 모서리를 시작하고 끝낼 좌표를 계산합니다.
            // 꼬리 높이(th) 대비 둥글기(ttr)의 비율을 구합니다.
            val ratio = ttr / th

            // 곡선이 시작되는 X, Y 좌표 (꼭짓점 도달 전)
            val curveStartX = tailTipX - (tailTipX - tailLeftX) * ratio
            val curveStartY = ttr

            // 곡선이 끝나는 X, Y 좌표 (꼭짓점을 넘어선 후)
            val curveEndX = tailTipX + (tailRightX - tailTipX) * ratio
            val curveEndY = ttr

            // 좌상단부터 시계방향으로 그립니다.
            moveTo(bodyLeft + cr, bodyTop)

            // 윗변과 꼬리
            lineTo(tailLeftX, bodyTop)

            // 1. 꼭짓점 바로 아래(curveStartY)까지 직선을 긋습니다.
            lineTo(curveStartX, curveStartY)

            // 2. 원래의 뾰족한 끝(tailTipX, 0f)을 제어점으로 삼아 부드러운 곡선을 그립니다.
            quadraticBezierTo(
                x1 = tailTipX, y1 = 0f, // 꺾이는 기준점 (원래의 뾰족한 끝)
                x2 = curveEndX, y2 = curveEndY // 곡선이 도착할 점
            )

            // 3. 꼬리 오른쪽 밑동으로 내려옵니다.
            lineTo(tailRightX, bodyTop)

            lineTo(bodyRight - cr, bodyTop)

            // 우상단 모서리
            arcTo(Rect(bodyRight - 2*cr, bodyTop, bodyRight, bodyTop + 2*cr), -90f, 90f, false)
            // 우측변
            lineTo(bodyRight, bodyBottom - cr)
            // 우하단 모서리
            arcTo(Rect(bodyRight - 2*cr, bodyBottom - 2*cr, bodyRight, bodyBottom), 0f, 90f, false)
            // 아랫변
            lineTo(bodyLeft + cr, bodyBottom)
            // 좌하단 모서리
            arcTo(Rect(bodyLeft, bodyBottom - 2*cr, bodyLeft + 2*cr, bodyBottom), 90f, 90f, false)
            // 좌측변
            lineTo(bodyLeft, bodyTop + cr)
            // 좌상단 모서리
            arcTo(Rect(bodyLeft, bodyTop, bodyLeft + 2*cr, bodyTop + 2*cr), 180f, 90f, false)

            close() // 도형 닫기
        })
    }
}

/**
 * 홈 화면 카드 위에 표시되는 말풍선.
 * 최대 2줄까지 표시하며, 2줄 안에 다 들어가지 않으면 글자 크기를 자동으로 줄인다.
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

    val baseStyle = MaterialTheme.appTypography.bodyMedium16_h22.copy(color = theme.textPrimary)
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
private val MIN_BUBBLE_FONT_SIZE = 12.sp
