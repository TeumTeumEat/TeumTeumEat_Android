package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme


@Composable
fun CheckToggleButton(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isShowedIcon: Boolean = true,
) {
    val toggleWidth = 64.dp
    val toggleHeight = 36.dp
    val knobSize = 28.dp
    val materialTheme = MaterialTheme.colorScheme

    val offsetX by animateDpAsState(
        targetValue = if (checked) toggleWidth - knobSize - 4.dp else 4.dp,
        label = "toggleOffset"
    )

    Box(
        modifier = modifier
            .size(toggleWidth, toggleHeight)
            .clip(RoundedCornerShape(50))
            .background(if (checked) materialTheme.primary else materialTheme.secondaryContainer)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
    ) {

        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .size(knobSize)
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isShowedIcon){
                Icon(
                    imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (checked) materialTheme.primary else materialTheme.secondaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CheckToggleButtonThemePreview() {
    TeumTeumEatTheme {
        var checked by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CheckToggleButton(
                checked = checked,
                onCheckedChange = { checked = it },
                isShowedIcon = false
            )
        }
    }
}

