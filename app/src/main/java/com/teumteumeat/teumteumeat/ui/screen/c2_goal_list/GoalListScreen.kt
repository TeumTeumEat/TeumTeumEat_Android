package com.teumteumeat.teumteumeat.ui.screen.c2_goal_list

import android.util.Log
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
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
import com.teumteumeat.teumteumeat.ui.component.modal.BaseModal
import com.teumteumeat.teumteumeat.ui.component.mypage.TagChip
import com.teumteumeat.teumteumeat.ui.screen.c1_mypage.MyPageActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun GoalListScreen(
    uiState: UiStateGoalList,
    onBackClick: () -> Unit,
    onGoalClick: (Int) -> Unit,
    onCancelChangeGoal: () -> Unit,
    onConfirmChangeGoal: () -> Unit,
    onRetryApi: () -> Unit,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography
    val screenState = LocalScreenState.current
    val activity = LocalActivityContext.current as GoalListActivity

    DefaultMonoBg() {
        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            topBar = {
                TitleBar(
                    "학습 주제 설정",
                    onBackClick = onBackClick,
                )
            }
        ) { padding ->

            when (screenState) {
                is UiScreenState.Error -> {

                    val errorMessage = screenState.message

                    FullScreenErrorModal(
                        errorState = ErrorState(
                            title = "에러가 발생했습니다.",
                            description = errorMessage,
                            retryLabel = "다시 시도하기",
                            onRetry = onRetryApi
                        ),
                        onBack = {
                            Utils.UxUtils.moveActivity(
                                activity,
                                MyPageActivity::class.java
                            )
                        },
                    )

                }

                UiScreenState.Idle, UiScreenState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LoadingScreen(
                            title = "학습 주제 불러오는 중",
                            message = "잠시만 기다려주세요"
                        )
                    }
                }

                UiScreenState.Success -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
                                .background(theme.backgroundW100)
                        ) {


                            Spacer(modifier = Modifier.height(8.dp))

                            // 🔹 선택된 목표를 최상단으로, 그 다음 만료되지 않은 순으로 정렬
                            val sortedGoals = uiState.goals.sortedWith(
                                compareByDescending<GoalCardUiModel> { it.isSelected } // 1순위: 선택 여부
                                    .thenBy { it.isExpired } // 2순위: 만료 안 된 것 우선 (false < true)
                            )

                            // 정렬 후 결과 확인
                            Log.d("GoalDebug", "--- 정렬 완료 ---")
                            sortedGoals.forEachIndexed { index, goal ->
                                Log.d(
                                    "GoalDebug",
                                    "[$index] ID: ${goal.goalId} | Selected: ${goal.isSelected}"
                                )
                            }

                            // 🔹 목표 리스트
                            sortedGoals.forEach { goal ->
                                GoalCard(
                                    uiModel = goal,
                                    onClick = { onGoalClick(goal.goalId) }
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // 🔹 오버레이
                        if (uiState.showChangeGoalOverlay) {
                            ChangeGoalOverlay(
                                onCancel = onCancelChangeGoal,
                                onConfirm = onConfirmChangeGoal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChangeGoalOverlay(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        BaseModal(
            title = "주제를 변경할까요?",
            body = null,
            primaryButtonText = "변경하기",
            secondaryButtonText = "다시 고르기",
            onPrimaryClick = onConfirm,
            onSecondaryClick = onCancel
        )
    }
}

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
                    enabled = if (BuildConfig.DEBUG) true else !uiModel.isExpired && !uiModel.isSelected,
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
        if (uiModel.isExpired) {
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
            ){
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.End
                ){
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



