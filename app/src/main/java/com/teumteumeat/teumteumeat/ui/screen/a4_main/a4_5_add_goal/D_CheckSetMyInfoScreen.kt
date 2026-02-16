package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.mapDifficultyToKorean
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingViewModel
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.UiStateOnboardingState
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.toDisplayText
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun AddGoalCheckSetMyInfoScreen(
    viewModel: AddGoalViewModel,
    uiState: UiStateAddGoalState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    val bottomFixedHeight = 170.dp // ✅ 그라데이션 + 버튼 영역
    DefaultMonoBg(
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
                        .padding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))
                    Text(
                        "널 뭐라고 불러줄까?",
                        style = Typography.headlineMedium.copy(
                            fontSize = 18.sp,
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Image(
                        painter = painterResource(R.drawable.character_front),
                        contentDescription = "앞을 보는 케릭터",
                        modifier = Modifier.size(width = 200.dp, height = 162.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(25.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = bottomFixedHeight), // ✅ 핵심,
                    ) {



// ===== 5. 정보입력 주제 지정 =====
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                text = if(uiState.goalTypeUiState == GoalTypeUiState.CATEGORY) "관심분야"
                                    else "문서이름",
                                style = Typography.bodyLarge.copy(fontSize = 18.sp)
                            )
                        }

// GoalType에 따라 표시 내용 분기 처리
                        val subjectText = when (uiState.goalTypeUiState) {
                            GoalTypeUiState.DOCUMENT -> uiState.selectedFileName.ifEmpty { "선택된 파일 없음" }
                            GoalTypeUiState.CATEGORY -> {
                                // 앱개발 > ios > swift 형식으로 결합
                                listOfNotNull(
                                    uiState.categorySelection.depth1?.name ?: "IT",
                                    uiState.categorySelection.depth2?.name,
                                    uiState.categorySelection.depth3?.name,
                                    uiState.categorySelection.depth4?.name,
                                ).joinToString(" > ").ifEmpty { "선택된 카테고리 없음" }
                            }

                            else -> "선택 안함"
                        }
                        BaseOutlineButton(
                            text = subjectText,
                            contentAligment = Alignment.Center,
                            isEnabled = false,
                            textStyle = MaterialTheme.appTypography.btnSemiBold18_h24.copy(
                                fontSize = 18.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.extendedColors.unableContent,
                            ),
                            btnHeight = 50,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { /* 클릭 시직 정의 필요 시 추가 */ },
                            maxLine = 1,
                            overFlowSetting = TextOverflow.Ellipsis
                        )

// ===== 6. 난이도 =====
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                text = "난이도",
                                style = Typography.bodyLarge.copy(fontSize = 18.sp)
                            )
                        }

// Difficulty Enum 값을 한글 표기 등으로 변환 필요 (여기서는 name 사용하거나 별도 매핑)
                        BaseOutlineButton(
                            text = mapDifficultyToKorean(uiState.difficulty),  // 필요하다면 별도 mapper 함수 사용 (ex: Difficulty.MIDDLE -> "중")
                            contentAligment = Alignment.Center,
                            isEnabled = false,
                            textStyle = MaterialTheme.appTypography.btnSemiBold18_h24.copy(
                                fontSize = 18.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.extendedColors.unableContent,
                            ),
                            btnHeight = 50,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { /* 클릭 로직 */ }
                        )


// ===== 7. 프롬프트 =====
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                text = "프롬프트",
                                style = Typography.bodyLarge.copy(fontSize = 18.sp)
                            )
                        }

                        val promptText =
                            if (uiState.promptInput.isBlank()) "입력 안함" else uiState.promptInput

                        BaseOutlineButton(
                            text = promptText,
                            contentAligment = Alignment.Center,
                            isEnabled = false,
                            textStyle = MaterialTheme.appTypography.btnSemiBold18_h24.copy(
                                fontSize = 18.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.extendedColors.unableContent,
                            ),
                            btnHeight = 50,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { /* 클릭 로직 */ },
                            maxLine = 2,
                            overFlowSetting = TextOverflow.Ellipsis
                        )

// === 8.공부기간 ====
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                "공부기간",
                                style = Typography.bodyLarge.copy(
                                    fontSize = 18.sp,
                                )
                            )
                        }

                        val studyWeekText = uiState.studyPeriod?.let { "${it}주" } ?: "기간 설정 안함"

                        BaseOutlineButton(
                            text = studyWeekText,
                            contentAligment = Alignment.Center,
                            isEnabled = false,
                            textStyle = MaterialTheme.appTypography.btnSemiBold18_h24.copy(
                                fontSize = 18.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.extendedColors.unableContent,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            btnHeight = 50,
                            onClick = {}
                        )


                    }
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "수정이 필요하면\n" +
                                "왼쪽 상단의 < 돌아가기를 눌러주세요",
                        style = MaterialTheme.appTypography.captionRegular14.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.extendedColors.textOnUnselected,
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = Typography.labelMedium.copy(
                            lineHeight = 24.sp
                        ),
                        isEnabled = true,
                        onClick = {
                            // 각 usecase 진행하면서 에러가 발생하면 뷰에 에러화면 표시
                            viewModel.submitOnBoarding()
                        },
                        conerRadius = 16.dp
                    )
                }

            }
        },
    )
}

