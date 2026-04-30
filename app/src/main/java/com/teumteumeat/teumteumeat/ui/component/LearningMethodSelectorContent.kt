package com.teumteumeat.teumteumeat.ui.component

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.component.radio_group.BoxButtonRadioGroup
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.ui.theme.Typography

/**
 * 학습 방식 선택 UI — 온보딩과 목표 추가 화면이 공유하는 컴포넌트.
 *
 * @param selectedType 현재 선택된 학습 방식
 * @param onSelected 라디오 버튼 선택 콜백
 * @param onNextClick "다음으로" 버튼 클릭 콜백 (라우팅·사이드이펙트는 호출부에서 처리)
 */
@Composable
fun LearningMethodSelectorContent(
    selectedType: GoalTypeUiState,
    onSelected: (GoalTypeUiState) -> Unit,
    onNextClick: () -> Unit,
) {
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
                    SpeechBubble(text = "준비된 주제로 시작할까요,\n직접 자료를 가져올까요?")
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_sel_learning_method),
                        contentDescription = "책을 보며 말하는 케릭터",
                    )
                    Spacer(modifier = Modifier.height(15.dp))

                    BoxButtonRadioGroup(
                        selectedType = selectedType,
                        onSelected = onSelected,
                    )

                    Spacer(Modifier.height(108.dp))
                }

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
                        textStyle = Typography.labelMedium.copy(lineHeight = 24.sp),
                        isEnabled = selectedType != GoalTypeUiState.NONE,
                        onClick = onNextClick,
                        conerRadius = 16.dp,
                    )
                }
            }
        },
    )
}

@Preview(showBackground = true, name = "선택 없음")
@Composable
private fun LearningMethodSelectorContentPreviewNone() {
    TeumTeumEatTheme {
        var selected by remember { mutableStateOf(GoalTypeUiState.NONE) }
        LearningMethodSelectorContent(
            selectedType = selected,
            onSelected = { selected = it },
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, name = "카테고리 선택")
@Composable
private fun LearningMethodSelectorContentPreviewCategory() {
    TeumTeumEatTheme {
        var selected by remember { mutableStateOf(GoalTypeUiState.CATEGORY) }
        LearningMethodSelectorContent(
            selectedType = selected,
            onSelected = { selected = it },
            onNextClick = {},
        )
    }
}

@Preview(showBackground = true, name = "문서 선택")
@Composable
private fun LearningMethodSelectorContentPreviewDocument() {
    TeumTeumEatTheme {
        var selected by remember { mutableStateOf(GoalTypeUiState.DOCUMENT) }
        LearningMethodSelectorContent(
            selectedType = selected,
            onSelected = { selected = it },
            onNextClick = {},
        )
    }
}