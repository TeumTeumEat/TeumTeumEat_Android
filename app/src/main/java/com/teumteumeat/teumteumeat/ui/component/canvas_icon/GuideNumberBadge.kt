package com.teumteumeat.teumteumeat.ui.component.canvas_icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun NumberBadge(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    backgroundColor: Color = MaterialTheme.extendedColors.primary, // 디자인 기준 블루
    textColor: Color = MaterialTheme.extendedColors.textOnPrimary,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontSize = (size.value * 0.5f).sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NumberBadgePreview() {
    TeumTeumEatTheme {
        NumberBadge(number = 1)
    }
}