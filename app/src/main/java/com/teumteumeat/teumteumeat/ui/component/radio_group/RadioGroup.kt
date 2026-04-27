package com.teumteumeat.teumteumeat.ui.component.radio_group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.component.button.SelectableBoxButton
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.DifficultyOption
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

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
                btnHeight = 50,
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
fun QuestionCountRadioGroup(
    modifier: Modifier = Modifier,
    options: List<Int>,
    selectedQuestionCnt: Int?,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { questionCount ->
                    val isSelected = questionCount == selectedQuestionCnt
                    BaseOutlineButton(
                        modifier = Modifier.weight(1f),
                        btnHeight = 50,
                        text = "${questionCount}문제",
                        textStyle = MaterialTheme.appTypography.btnSemiBold18_h24.copy(
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.extendedColors.textGhost
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { onSelect(questionCount) }
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
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
        val itemWidth = (maxWidth-12.dp) / 2

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                12.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {

            // 📦 카테고리 선택
            SelectableBoxButton(
                modifier = Modifier
                    .width(itemWidth)
                    .padding(vertical = 29.dp, horizontal = 14.dp),
                isSelected = selectedType == GoalTypeUiState.CATEGORY,
                titleText = "카테고리 선택",
                labelText = "공부하고 싶은 걸\n골라볼게요",
                iconRes = R.drawable.icon_category_fill,
                onClick = {
                    onSelected(GoalTypeUiState.CATEGORY)
                }
            )

            // 📦 파일 업로드
            SelectableBoxButton(
                modifier = Modifier
                    .width(itemWidth)
                    .padding(vertical = 27.dp, horizontal = 14.dp),
                isSelected = selectedType == GoalTypeUiState.DOCUMENT,
                titleText = "파일 업로드",
                labelText = "공부하고 싶은\n내용이 있어요",
                iconRes = R.drawable.icon_file_fill,
                onClick = {
                    onSelected(GoalTypeUiState.DOCUMENT)
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun BoxButtonRadioGroupPreview() {
    // 프리뷰 내에서 버튼 클릭 동작을 테스트하기 위한 임시 상태입니다.
    // 기본값으로 DOCUMENT를 선택해 둡니다.
    var selectedType by remember { mutableStateOf(GoalTypeUiState.CATEGORY) }

    // 실제 디자인 시스템이나 테마가 있다면 여기에 감싸주면 더 정확하게 보입니다. (예: MaterialTheme)
    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 20.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            BoxButtonRadioGroup(
                selectedType = selectedType,
                onSelected = { newSelectedType ->
                    // 버튼이 클릭되면 상태를 업데이트하여 UI를 다시 그립니다.
                    selectedType = newSelectedType
                }
            )
        }
    }
}





