package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun CheckBoxCircle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val materialTheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                color = if (checked) Color(0xFF2B8FFF) else materialTheme.onPrimary,
                shape = RoundedCornerShape(16.dp),

            )
            .border(
                width = if (checked) 0.dp else 2.dp, // 체크 안 되었을 때만 테두리
                color = materialTheme.secondaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                indication = null, // 클릭 깜박임 제거
                interactionSource = remember { MutableInteractionSource() }
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Checked",
                tint = materialTheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomCheckBoxPreview() {
    var checked by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CheckBoxCircle(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}
