package com.teumteumeat.teumteumeat.ui.component.canvas_icon

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun XOutlineIcon(
    size: Dp = 25.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = Color(0xFFFF3B30) // 기본 빨간색
) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(size)
    ) {
        val strokePx = strokeWidth.toPx()

        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.toPx(), size.toPx()),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(size.toPx(), 0f),
            end = Offset(0f, size.toPx()),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )
    }
}
