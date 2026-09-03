package com.teumteumeat.teumteumeat.ui.component.modal.bubble

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.Black100
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.ui.theme.White100
import com.teumteumeat.teumteumeat.utils.appTypography

/**
 * 홈 화면 상단 스트릭 아이콘 아래에 표시되는 리그 진입 유도 말풍선.
 * [HomeSpeechBubble]과 동일한 [SpeechBubbleShape] 꼬리 도형을 재사용하되,
 * 검은 배경 + 흰 텍스트로 강조되는 스타일을 사용한다.
 */
@Composable
fun LeagueSpeechBubble(
    modifier: Modifier = Modifier,
    text: String = "1등 도전! 👑",
    backgroundColor: Color = Black100,
    textColor: Color = White100,
    shadowElevation: Dp = 6.dp,
    // Figma의 corner radius "999"와 동일한 값. SpeechBubbleShape 내부에서 본체 높이의
    // 절반으로 clamp되므로, 실제 렌더링 높이와 무관하게 항상 완전한 필(pill) 형태가 된다.
    cornerRadius: Dp = 999.dp,
    tailWidth: Dp = 14.dp,
    tailHeight: Dp = 8.dp,
    tailPaddingEnd: Dp = 24.dp,
    // 꼬리 끝의 뾰족한 정도. 값이 작을수록 뾰족하고, tailHeight에 가까워질수록 뭉툭해진다.
    tailTipRadius: Dp = 0.dp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bubbleShape = SpeechBubbleShape(cornerRadius, tailWidth, tailHeight, tailPaddingEnd, tailTipRadius)

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .shadow(elevation = shadowElevation, shape = bubbleShape, clip = false)
            .background(color = backgroundColor, shape = bubbleShape)
            .padding(top = tailHeight)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.appTypography.bodyMedium14.copy(color = textColor),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F4F6)
@Composable
private fun PreviewLeagueSpeechBubble() {
    TeumTeumEatTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // 뾰족한 꼬리 (tailHeight에 비해 tailTipRadius가 작음)
            LeagueSpeechBubble(text ="1등 도전! 👑", onClick = {})
            // 뭉툭한 꼬리 (tailTipRadius가 tailHeight에 가까움)
            LeagueSpeechBubble(onClick = {}, tailTipRadius = 8.dp,)
        }
    }
}
