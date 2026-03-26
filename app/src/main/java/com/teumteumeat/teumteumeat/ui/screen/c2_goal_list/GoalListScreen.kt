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
import com.teumteumeat.teumteumeat.ui.component.card.GoalCard
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
                                    .thenBy { it.isCompleted } // 2순위: 만료 안 된 것 우선 (false < true)
                            )

                            // 정렬 후 결과 확인
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




