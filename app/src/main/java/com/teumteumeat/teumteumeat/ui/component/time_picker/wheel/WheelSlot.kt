package com.teumteumeat.teumteumeat.ui.component.time_picker.wheel

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun WheelSlot(
    modifier: Modifier = Modifier,
    minWidth: Dp,
    horizontalPadding: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .widthIn(min = minWidth)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
