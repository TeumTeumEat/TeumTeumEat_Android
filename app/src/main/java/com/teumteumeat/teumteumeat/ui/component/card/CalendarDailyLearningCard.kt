package com.teumteumeat.teumteumeat.ui.component.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.domain.model.common.DomainGoalType_v1
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@Composable
fun CalendarDailyLearningCard(
    title: String,
    description: String,
    dateText: String, // ex) "01.03"
    domainGoalTypeV1: DomainGoalType_v1,      // ✅ 추가
    onClick: () -> Unit,
) {

    // ✅ 타입별 UI 분기 포인트
    val typeLabel = when (domainGoalTypeV1) {
        DomainGoalType_v1.CATEGORY -> "카테고리"
        DomainGoalType_v1.DOCUMENT -> "문서"
    }

    TeumTeumEatTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.extendedColors.btnFillSecondary,
            onClick = onClick          // ✅ 클릭 + 기본 리플
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                // 🔹 상단: 제목 + 날짜
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.appTypography.subtitleSemiBold16,
                        modifier = Modifier.weight(1f)
                    )

                    // ✅ 타입 표시 (카테고리 / 문서)
                    if (false) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${typeLabel}학습",
                            style = MaterialTheme.appTypography.captionRegular12,
                            color = MaterialTheme.extendedColors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                    }


                    Text(
                        text = dateText,
                        style = MaterialTheme.appTypography.captionRegular12,
                        color = MaterialTheme.extendedColors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 🔹 설명 (최대 2줄)
                Text(
                    text = description,
                    style = MaterialTheme.appTypography.captionRegular12,
                    color = MaterialTheme.extendedColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(
    name = "LibraryHistoryCard Preview",
    showBackground = true
)
@Composable
fun LibraryHistoryCardPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .padding(20.dp)
        ) {
            CalendarDailyLearningCard(
                title = "오늘의 퀴즈 학습",
                description = "개념에 대한 간단한 요약입니다 어떻게 보여줄지는 논의 해봐야할듯 1줄? 2줄? 저는 많아도 2줄이 적당한 것 같은데, 개념에 대한 간단한 요약입니다 어떻게 보여줄지는 논의 해봐야할듯 1줄? 2줄? 저는 많아도 2줄이 적당한 것 같은데",
                dateText = "01.03",
                onClick = { },
                domainGoalTypeV1 = DomainGoalType_v1.DOCUMENT,
            )
        }
    }
}
