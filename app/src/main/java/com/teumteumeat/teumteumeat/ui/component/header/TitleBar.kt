package com.teumteumeat.teumteumeat.ui.component.header

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun TitleBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 56,
) {
    val theme = MaterialTheme.extendedColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = theme.unableContainer,
                    start = Offset(0f, size.height - strokeWidth),
                    end = Offset(size.width, size.height - strokeWidth),
                    strokeWidth = strokeWidth
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 🔹 왼쪽: 뒤로가기 버튼
        Box(
            modifier = Modifier
                .size(iconSize.dp), // ⭐ 오른쪽과 동일한 공간 확보
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = "previous page",
                    tint = theme.iconBlack
                )
            }
        }

        // 🔹 가운데: 타이틀 (진짜 중앙)
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.appTypography.subtitleSemiBold20
            )
        }

        // 🔹 오른쪽: 더미 공간 (중앙 정렬 보정용)
        Box(
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Preview(
    name = "TitleBar Full Screen Preview",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
private fun TitleBarFullScreenPreview() {
    TeumTeumEatTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                TitleBar(
                    title = "오늘의 냠냠지식",
                    onBackClick = {}
                )
            }
        }
    }
}

