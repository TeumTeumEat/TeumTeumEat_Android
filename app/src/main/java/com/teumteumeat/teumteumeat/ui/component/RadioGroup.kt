package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MinuteRadioGroup(
    modifier: Modifier = Modifier,
    options: List<Int>,
    selectedMinute: Int?,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { minute ->
            val isSelected = minute == selectedMinute

            BaseOutlineButton(
                text = if(minute == options.lastIndex) "${minute}분 +" else "${minute}분",
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary   // ✅ 선택됨 (파란색)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant // ❌ 미선택
                },
                onClick = {
                    onSelect(minute)
                }
            )
        }
    }
}