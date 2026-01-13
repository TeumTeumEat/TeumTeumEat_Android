package com.teumteumeat.teumteumeat.ui.screen.a4_main.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun FloatingActionItem(
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier
            .size(56.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = backgroundColor,
        shadowElevation = 6.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
