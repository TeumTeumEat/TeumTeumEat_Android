package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.StudyWeekOption
import com.teumteumeat.teumteumeat.utils.appTypography

@Composable
fun WeekRadioGroup(
    modifier: Modifier = Modifier,
    options: List<StudyWeekOption>,
    selectedValue: Int?,
    onSelect: (StudyWeekOption) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            val isSelected = option.value == selectedValue

            BaseOutlineButton(
                text = option.label,
                textStyle = MaterialTheme.appTypography.btnSemiBold18_h24.copy(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                onClick = {
                    onSelect(option)
                },
            )
        }
    }
}