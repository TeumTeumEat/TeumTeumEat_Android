import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
fun PreviewMainScreenWithBubble() {
    var isQuizCompleted by remember { mutableStateOf(false) }

    val bubbleScale by animateFloatAsState(
        targetValue = if (isQuizCompleted) 1.0f else 0f, // 크기는 이제 1.0에서 멈춥니다 (테두리가 애니메이션 되므로)
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
                        GlowingSpeechBubble(
                            text = "음냐냐.. 퀴즈 더 풀고 싶다아~ Click!",
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

@Composable
fun GlowingSpeechBubble(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = MaterialTheme.extendedColors.btnFillDisabled,
    glowColor: Color = Color(0xFFFFD700), // 테두리 네온 색상
    cornerRadius: Dp = 20.dp,
    tailWidth: Dp = 19.dp,
    tailHeight: Dp = 14.dp,
    tailPaddingEnd: Dp = 32.dp,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val theme = MaterialTheme.extendedColors

    // 💡 테두리 투명도를 조절하는 숨쉬기(Pulse) 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "glow_transition")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f, // 가장 어두울 때의 투명도
        targetValue = 1.0f,  // 가장 밝을 때의 투명도
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse // 밝아졌다 어두워졌다 반복
        ),
        label = "glow_alpha"
    )

    val interactionSource = remember { MutableInteractionSource() }

    // 위에서 만든 커스텀 도형 객체 생성
    val bubbleShape = SpeechBubbleShape(cornerRadius, tailWidth, tailHeight, tailPaddingEnd)

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null, // 리플(클릭) 이펙트 제거
                onClick = {
                    onClick()
                }
            )
            // 🌟 이 도형의 외곽선(꼬리 포함)을 따라 빛나는 테두리를 그립니다.
            .border(
                width = 2.dp, // 테두리 두께 조절
                color = glowColor.copy(alpha = glowAlpha), // 애니메이션되는 투명도 적용
                shape = bubbleShape
            )
            // 같은 도형으로 배경색 칠하기
            .background(color = backgroundColor, shape = bubbleShape)
            // 내용물이 꼬리 영역을 침범하지 않도록 상단에 패딩 추가
            .padding(top = tailHeight)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.appTypography.bodyMedium14
                .copy(color = theme.textOnPrimary),
            textAlign = TextAlign.Center
        )
    }
}