package com.teumteumeat.teumteumeat.ui.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.domain.model.common.DomainGoalType_v1
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun TopicCategoryCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val selectedColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.extendedColors.backgroundW100,
        border =
            BorderStroke(
                width = 2.dp,
                color = if (isSelected) selectedColor
                    else  MaterialTheme.extendedColors.btnLineDisable
            ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {

            Text(
                text = title,
                style = MaterialTheme.appTypography.subtitleSemiBold16,
                color = if (isSelected) {
                    selectedColor
                } else {
                    MaterialTheme.extendedColors.textPrimary
                },
                modifier = Modifier.weight(1f)
            )

            // 토글 아이콘
            Icon(
                imageVector = if (isSelected) {
                    Icons.Rounded.KeyboardArrowUp
                } else {
                    Icons.Rounded.KeyboardArrowDown
                },
                contentDescription = if (isSelected) {
                    "collapse category"
                } else {
                    "expand category"
                },
                tint = if (isSelected) {
                    selectedColor
                } else {
                    MaterialTheme.extendedColors.textSecondary
                }
            )

        }
    }
}

private data class MockHistory(
    val id: Long,
    val title: String,
    val description: String,
    val dateText: String,
)

private val mockCategories = listOf(
    "Kotlin" to listOf(
        MockHistory(1, "Kotlin 언어 개요 및 퀴즈", "Kotlin 개요 Kotlin은 안드로이드 개발...", "01.10"),
        MockHistory(2, "Kotlin 기초와 트렌드", "Kotlin 기초 개념 및 최신 트렌드...", "01.07"),
        MockHistory(3, "Kotlin 핵심 개념 요약", "Kotlin 핵심 개념 요약 Kotlin 소개...", "01.05"),
    ),
    "UX 디자인" to listOf(
        MockHistory(4, "휴리스틱 평가 개요", "사용성 평가의 대표 기법...", "01.03")
    )
)

@Composable
fun TopicTabMockPreviewContent() {

    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        mockCategories.forEach { (categoryName, histories) ->
            item {

                Column {

                    // ✅ 주제 카드
                    TopicCategoryCard(
                        title = categoryName,
                        isSelected = selectedCategory == categoryName,
                        onClick = {
                            selectedCategory =
                                if (selectedCategory == categoryName) null
                                else categoryName
                        }
                    )

                    // ✅ 선택된 경우에만 학습 카드 노출
                    if (selectedCategory == categoryName) {
                        Spacer(modifier = Modifier.height(12.dp))

                        histories.forEach { history ->
                            CalendarDailyLearningCard(
                                title = history.title,
                                description = history.description,
                                dateText = history.dateText,
                                domainGoalTypeV1 = DomainGoalType_v1.CATEGORY,
                                onClick = {}
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopicTabMockPreview() {
    TeumTeumEatTheme {
        TopicTabMockPreviewContent()
    }
}

