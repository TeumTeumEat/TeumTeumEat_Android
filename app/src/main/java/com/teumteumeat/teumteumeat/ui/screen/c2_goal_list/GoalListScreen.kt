package com.teumteumeat.teumteumeat.ui.screen.c2_goal_list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageAccountSection
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageArrowRow
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageNavigateBox
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageRow
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageTitleRow
import com.teumteumeat.teumteumeat.ui.component.mypage.MyPageToggleRow
import com.teumteumeat.teumteumeat.ui.component.mypage.SelectedTopicSection
import com.teumteumeat.teumteumeat.ui.component.mypage.TagChip
import com.teumteumeat.teumteumeat.ui.screen.c1_mypage.UiStateMyPage
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun GoalListScreen(
    uiState: UiStateGoalList,
    onBackClick: () -> Unit,
    onGoalClick: (Int) -> Unit,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = "back"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "학습 주제 설정",
                    style = MaterialTheme.appTypography.subtitleSemiBold20
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(theme.backgroundW100)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 로딩 상태
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .align(Alignment.CenterHorizontally)
                )
                return@Column
            }

            // 🔹 에러 상태
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    modifier = Modifier
                        .padding(20.dp)
                        .align(Alignment.CenterHorizontally)
                )
                return@Column
            }

            // 🔹 목표 리스트
            uiState.goals.forEach { goal ->
                GoalCard(
                    uiModel = goal,
                    onClick = { onGoalClick(goal.goalId) }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun GoalCard(
    modifier: Modifier = Modifier,
    uiModel: GoalCardUiModel,
    onClick: (Int) -> Unit,
) {
    val theme = MaterialTheme.extendedColors

    Box(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .border(
                width = if (uiModel.isSelected) 2.dp else 1.dp,
                color = if (uiModel.isSelected) theme.primary else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (uiModel.isSelected)
                    theme.primaryContainer
                else
                    theme.backSurface,
                shape = RoundedCornerShape(12.dp)
            )
            // .clickable { onClick(uiModel.goalId) }
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
            if(uiModel.description.isNotBlank()){
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
}



