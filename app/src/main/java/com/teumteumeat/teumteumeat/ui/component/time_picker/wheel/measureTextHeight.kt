package com.teumteumeat.teumteumeat.ui.component.time_picker.wheel

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.teumteumeat.teumteumeat.ui.component.time_picker.PickTimeTextStyle

internal fun measureTextHeight(style: PickTimeTextStyle): Float {
    val paint = Paint().asFrameworkPaint().apply {
        textSize = style.fontSize.value
        isAntiAlias = true
    }
    return paint.fontMetrics.run { bottom - top }
}

internal fun measureTextWidth(
    text: String,
    style: PickTimeTextStyle
): Float {
    val paint = Paint().asFrameworkPaint().apply {
        textSize = style.fontSize.value
        isAntiAlias = true
    }
    return paint.measureText(text)
}
