package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.radio_group.BoxButtonRadioGroup
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.theme.Typography


@Composable
fun SelectInputMethodScreen(
    name: String = "",
    onPrev: () -> Unit,
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
    onNextFileUpload: () -> Unit,
    onNextCateGorySelect: () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    DefaultMonoBg(
        extensionHeight = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding()
                        .verticalScroll(rememberScrollState()),

                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    SpeechBubble(text = "제가 준비한 주제로 공부하시겠어요,\n" +
                            "원하는 자료를 공부하실래요?")
                    Spacer(modifier = Modifier.height(20.dp))
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_sel_learning_method),
                        contentDescription = "앞을 보는 케릭터",
                        modifier = Modifier.size(width = 200.dp, height = 162.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(25.dp))

                    BoxButtonRadioGroup(
                        selectedType = uiState.goalTypeUiState,
                        onSelected = { viewModel.selectLearningMethod(it) },
                    )

                    Spacer(Modifier.height(150.dp))

                }

                // 2️⃣ 하단 그라데이션 (페이드 효과)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = Typography.labelMedium.copy(
                            lineHeight = 24.sp
                        ),
                        isEnabled = uiState.goalTypeUiState != GoalTypeUiState.NONE,
                        onClick = {
                            viewModel.resetCategorySelection()
                            viewModel.onFileDeleted()
                            viewModel.updateCategorySelectionComplete(false) // 완료 상태 초기화
                            when(uiState.goalTypeUiState){
                                GoalTypeUiState.CATEGORY -> onNextCateGorySelect()
                                GoalTypeUiState.DOCUMENT -> onNextFileUpload()
                                GoalTypeUiState.NONE -> {}
                            }
                        },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}