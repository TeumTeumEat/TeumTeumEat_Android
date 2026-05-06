package com.teumteumeat.teumteumeat.ui.component.text

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle = LocalTextStyle.current,
    minFontSize: Float = 10f, // 최소 10sp까지 줄어들도록 설정
    textAlign: TextAlign = TextAlign.Center
) {
    // 현재 적용할 폰트 크기 상태 (기본값은 스타일의 크기 또는 16sp)
    var fontSizeValue by remember {
        mutableStateOf(if (baseStyle.fontSize.isUnspecified) 16.sp else baseStyle.fontSize)
    }
    // 텍스트를 그릴지 말지 결정 (크기 계산 중 깜빡임 방지)
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent() // 계산이 완료된 후에만 화면에 그림
        },
        style = baseStyle.copy(fontSize = fontSizeValue),
        maxLines = 2,
        softWrap = false,
        textAlign = textAlign,
        overflow = TextOverflow.Clip, // 넘치는 부분은 일단 자름
        onTextLayout = { textLayoutResult ->
            // 글자가 한 줄을 넘어가고(hasVisualOverflow), 최소 크기보다 크다면 크기를 줄임
            if (textLayoutResult.hasVisualOverflow && fontSizeValue.value > minFontSize) {
                fontSizeValue = (fontSizeValue.value * 0.9f).sp
            } else {
                readyToDraw = true // 적절한 크기를 찾았으므로 화면에 노출
            }
        }
    )
}