package com.teumteumeat.teumteumeat.ui.component.canvas_icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.AppColor
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun CircleOutlineIcon(
    size: Dp = 30.dp,
    strokeWidth: Dp = 4.dp,
    color: AppColor = MaterialTheme.extendedColors.primary
) {
    Canvas(
        modifier = Modifier.size(size)
    ) {
        drawCircle(
            color = color,
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}
