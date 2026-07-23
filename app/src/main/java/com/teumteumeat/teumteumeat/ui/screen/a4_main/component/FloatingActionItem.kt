package com.teumteumeat.teumteumeat.ui.screen.a4_main.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.text.AutoSizeText
import com.teumteumeat.teumteumeat.utils.appTypography

// ⭐ ExpandableAddMenu의 중앙 정렬 오프셋 계산에서도 참조하는 단일 크기 기준값
val FloatingActionItemSize = 65.dp

@Composable
fun FloatingActionItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {


    Surface(
        modifier = modifier
            .size(FloatingActionItemSize)
            .clip(CircleShape) // 1. 먼저 영역을 원형으로 자릅니다.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp), // ⭐ 상단에서 10dp 띄우기
            horizontalAlignment = Alignment.CenterHorizontally,
            // ⭐ 아이콘 하단 여백(2dp)만큼 텍스트와 겹치도록 음수 간격 적용
            verticalArrangement = Arrangement.spacedBy((-2).dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(31.dp) // ⭐ 원 크기와 무관하게 고정
            )

            AutoSizeText(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                baseStyle = MaterialTheme.appTypography.captionRegular12.copy(
                    fontWeight = FontWeight.Bold,
                    color = iconTint
                ),
                minFontSize = 8f,
            )
        }
    }
}
