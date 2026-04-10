package com.teumteumeat.teumteumeat.ui.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import com.teumteumeat.teumteumeat.ui.component.mypage.TagChip
import com.teumteumeat.teumteumeat.ui.screen.c2_goal_list.GoalCardUiModel
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.Utils.TypeUtils.toUiText


@Composable
fun GoalCard(
    modifier: Modifier = Modifier,
    uiModel: GoalCardUiModel,
    onClick: (Int) -> Unit,
) {
    val theme = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(12.dp)


    Box {
        Box(
            modifier = modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                // ⭐ 만료되지 않은 경우만 클릭 가능
                .clip(shape)
                .clickable(
                    enabled = if (BuildConfig.DEBUG) true else !uiModel.isCompleted && !uiModel.isSelected,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        onClick(uiModel.goalId)
                    }
                )
                .border(
                    width = if (uiModel.isSelected) 2.dp else 1.dp,
                    color = if (uiModel.isSelected) theme.primary else Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = when {
                        uiModel.isSelected -> theme.primaryContainer
                        else -> MaterialTheme.extendedColors.backgroundW100
                    },
                    shape = shape
                )
                .padding(16.dp)
        ) {

            Column {

                // 🔹 배지 영역
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagChip(text = uiModel.weekText)

                    if (uiModel.showDifficulty && uiModel.difficultyText.isNotBlank()) {
                        TagChip(text = uiModel.difficultyText)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 🔹 제목
                Text(
                    text = uiModel.title,
                    style = MaterialTheme.appTypography.bodyMedium16,
                    maxLines = 2
                )

                // 🔹 설명
                if (uiModel.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiModel.description,
                        style = MaterialTheme.appTypography.lableMedium12_h14,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }

            }
        }

        // 2. 컨텐츠 위에 씌울 검은색 반투명 오버레이
        // uiModel.isExpired 상황 등 특정 조건에서만 보여주고 싶다면 if문 사용
        if (uiModel.isCompleted || uiModel.isExpired) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .matchParentSize()
                    .clip(shape)
                    .border(
                        width = if (uiModel.isSelected) 2.dp else 1.dp,
                        color = if (uiModel.isSelected) theme.primary else Color.LightGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = Color.Black.copy(alpha = 0.4f), // 검은색 투명도 40%
                        shape = shape
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.End
                ) {
                    // 🔹 배지 영역
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = null,
                            tint = theme.textOnError,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            "만료된 주제",
                            style = MaterialTheme.appTypography.captionRegular14
                                .copy(
                                    color = theme.textOnError
                                )
                        )
                    }
                }
            }
        }
    }
}


// ... (기존 GoalCard 코드) ...

@Preview(
    showBackground = true,
    showSystemUi = true, // 전체화면(디바이스) 형태로 보여줌
    name = "GoalCard 상태별 전체화면 프리뷰"
)
@Composable
fun GoalCardStatesPreview() {
    // TeumTeumTheme 같은 전체 테마 래퍼가 있다면 여기 감싸주시면 더 좋습니다.
    TeumTeumEatTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5F5) // 배경색은 앱 테마에 맞게 조정 (예: 배경을 살짝 회색으로 해야 카드가 돋보임)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // 스크롤 가능하게 설정
                    .padding(vertical = 24.dp), // 상하 여백
                verticalArrangement = Arrangement.spacedBy(16.dp) // 카드 사이의 간격
            ) {
                // 1. 기본 상태 (미선택, 미완료)
                GoalCard(
                    uiModel = GoalCardUiModel(
                        goalId = 1,
                        weekText = "1주차",
                        difficulty = Difficulty.EASY,
                        difficultyText = Difficulty.HARD.toUiText(),
                        showDifficulty = true,
                        title = "매일 아침 물 한잔 마시기",
                        description = "아침에 일어나서 공복에 미지근한 물을 마십니다.",
                        isCompleted = false,
                        isSelected = false,
                        isExpired = false,
                    ),
                    onClick = {}
                )

                // 2. 선택된 상태 (isSelected = true)
                GoalCard(
                    uiModel = GoalCardUiModel(
                        goalId = 2,
                        weekText = "2주차",
                        difficulty = Difficulty.EASY,
                        difficultyText = Difficulty.HARD.toUiText(),
                        showDifficulty = true,
                        title = "하루 30분 걷기",
                        description = "점심시간이나 퇴근 후 가볍게 산책합니다.",
                        isCompleted = false,
                        isSelected = true,
                        isExpired = false
                    ),
                    onClick = {}
                )

                // 3. 만료된 상태 (isCompleted = true) - 검은색 오버레이 테스트
                GoalCard(
                    uiModel = GoalCardUiModel(
                        goalId = 3,
                        weekText = "3주차",
                        difficulty = Difficulty.MEDIUM,
                        difficultyText = Difficulty.HARD.toUiText(),
                        showDifficulty = true,
                        title = "만료된 목표입니다",
                        description = "기한이 지나 클릭할 수 없는 목표입니다.",
                        isCompleted = true,
                        isSelected = false,
                        isExpired = false,
                    ),
                    onClick = {}
                )

                // 3. 완료된 상태 (isCompleted = true) - 검은색 오버레이 테스트
                GoalCard(
                    uiModel = GoalCardUiModel(
                        goalId = 3,
                        weekText = "3주차",
                        difficulty = Difficulty.MEDIUM,
                        difficultyText = Difficulty.HARD.toUiText(),
                        showDifficulty = true,
                        title = "완료된 목표입니다",
                        description = "기한이 지나 클릭할 수 없는 목표입니다.",
                        isCompleted = true,
                        isSelected = true,
                        isExpired = false,
                    ),
                    onClick = {}
                )

                // 4. 설명(description)이 없는 기본 상태
                GoalCard(
                    uiModel = GoalCardUiModel(
                        goalId = 4,
                        weekText = "4주차",
                        difficulty = Difficulty.HARD,
                        difficultyText = Difficulty.HARD.toUiText(),
                        showDifficulty = true,
                        title = "설명이 없는 심플한 목표", // 설명란 비우기
                        description = "",
                        isCompleted = false,
                        isSelected = false,
                        isExpired = true,
                    ),
                    onClick = {}
                )
            }
        }
    }
}