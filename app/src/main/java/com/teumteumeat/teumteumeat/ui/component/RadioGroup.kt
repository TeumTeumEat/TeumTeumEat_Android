package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.component.button.SelectableBoxButton
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.DifficultyOption
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.utils.appTypography

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
                text = if (minute == options.last()) "${minute}분+" else "${minute}분",
                textStyle = MaterialTheme.appTypography.btnSemiBold18_h24.copy(
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                ),
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

@Composable
fun TextRadioGroup(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedOption: String?,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption

            BaseOutlineButton(
                text = option,
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
@Composable
fun DifficultyRadioGroup(
    modifier: Modifier = Modifier,
    options: List<DifficultyOption>,
    selected: Difficulty?,
    onSelect: (Difficulty) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected

            BaseOutlineButton(
                modifier = Modifier.weight(1f),
                text = option.label,
                textStyle = MaterialTheme.appTypography.btnSemiBold18_h24.copy(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ), // ✅ UI에는 "상/중/하"만 노출
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                onClick = {
                    onSelect(option.value) // ✅ ViewModel에는 enum만 전달
                }
            )
        }
    }
}




@Composable
fun BoxButtonRadioGroup(
    selectedType: GoalTypeUiState,
    onSelected: (GoalTypeUiState) -> Unit,
) {
    BoxWithConstraints {
        val itemWidth = maxWidth / 2

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // 📦 파일 업로드
            SelectableBoxButton(
                modifier = Modifier
                    .width(itemWidth)
                    .padding(vertical = 27.dp, horizontal = 14.dp),
                isSelected = selectedType == GoalTypeUiState.DOCUMENT,
                titleText = "파일 업로드",
                labelText = "공부하고 싶은\n내용이 있어요.",
                iconRes = R.drawable.icon_files,
                onClick = {
                    onSelected(GoalTypeUiState.DOCUMENT)
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 📦 카테고리 선택
            SelectableBoxButton(
                modifier = Modifier
                    .width(itemWidth)
                    .padding(vertical = 27.dp, horizontal = 14.dp),
                isSelected = selectedType == GoalTypeUiState.CATEGORY,
                titleText = "카테고리 선택",
                labelText = "공부하고 싶은 걸\n골라볼게요.",
                iconRes = R.drawable.icon_category,
                onClick = {
                    onSelected(GoalTypeUiState.CATEGORY)
                }
            )
        }
    }
}


