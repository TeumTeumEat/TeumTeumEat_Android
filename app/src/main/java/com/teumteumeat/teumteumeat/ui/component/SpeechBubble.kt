package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun SpeechBubble(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = MaterialTheme.extendedColors.btnFillSecondary,
    cornerRadius: Dp = 16.dp,
    tailWidth: Dp = 19.dp,
    tailHeight: Dp = 14.dp,
) {
    TeumTeumEatTheme {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 말풍선 본체
            Box(
                modifier = Modifier
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.appTypography.btnSemiBold20_h24.copy(
                        lineHeight = 28.sp
                    ),
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
            }

            // 🔻 말풍선 꼬리
            Canvas(
                modifier = Modifier
                    .size(
                        width = tailWidth,
                        height = tailHeight
                    )
            ) {
                drawBubbleTail(color = backgroundColor)
            }
        }
    }
}

private fun DrawScope.drawBubbleTail(
    color: Color
) {
    val path = Path().apply {
        moveTo(size.width / 2f, size.height)
        lineTo(0f, 0f)
        lineTo(size.width, 0f)
        close()
    }

    drawPath(
        path = path,
        color = color
    )
}

@Preview
@Composable
fun SpeechBubbleSample() {
    SpeechBubble(
        text = "틈틈잇에 오신 걸 환영해요!\n저는 틈틈이에요"
    )
}
