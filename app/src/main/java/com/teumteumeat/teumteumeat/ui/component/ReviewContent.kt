package com.teumteumeat.teumteumeat.ui.component

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.BaseOutlineButton
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

/**
 * 온보딩/목표 추가 플로우에서 공용으로 사용하는 등록 정보 확인 화면.
 *
 * @param speechBubbleText 상단 말풍선 텍스트
 * @param hintText 하단 버튼 위 안내 텍스트
 * @param subjectLabel "관심분야" 또는 "문서이름"
 * @param subjectText 관심분야/문서 표시 값
 * @param difficultyText 난이도 표시 값
 * @param promptText 프롬프트 표시 값
 * @param studyPeriodText 공부기간 표시 값
 * @param onNext 다음으로 버튼 클릭 콜백
 * @param leadingInfoContent 공통 섹션 앞에 추가할 섹션 (온보딩 전용: 학습 분량, 알림 시간)
 */
@Composable
fun ReviewContent(
    speechBubbleText: String,
    hintText: String,
    subjectLabel: String,
    subjectText: String,
    difficultyText: String,
    promptText: String,
    studyPeriodText: String,
    onNext: () -> Unit,
    leadingInfoContent: (@Composable () -> Unit)? = null,
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
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpeechBubble(text = speechBubbleText)
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(
                        painter = painterResource(R.drawable.char_onboarding_five_five),
                        contentDescription = "앞을 보는 캐릭터",
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 170.dp),
                    ) {
                        leadingInfoContent?.invoke()

                        ReviewInfoItem(
                            label = subjectLabel,
                            text = subjectText,
                            maxLines = 2,
                        )
                        ReviewInfoItem(
                            label = "난이도",
                            text = difficultyText,
                        )
                        ReviewInfoItem(
                            label = "프롬프트",
                            text = promptText,
                            maxLines = 2,
                        )
                        ReviewInfoItem(
                            label = "공부기간",
                            text = studyPeriodText,
                        )
                    }
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = hintText,
                        style = MaterialTheme.appTypography.captionRegular14.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.extendedColors.textOnUnselected,
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = MaterialTheme.appTypography.btnBold20_h24.copy(
                            color = MaterialTheme.extendedColors.backgroundW100
                        ),
                        isEnabled = true,
                        onClick = onNext,
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}

/**
 * 레이블 + 아웃라인 버튼으로 구성된 정보 표시 섹션 (공통/개별 모두 사용 가능).
 */
@Composable
fun ReviewInfoItem(
    label: String,
    text: String,
    maxLines: Int = 1,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = label,
            style = MaterialTheme.appTypography.subtitleSemiBold16,
        )
    }
    BaseOutlineButton(
        text = text,
        contentAligment = Alignment.Center,
        isEnabled = false,
        textStyle = MaterialTheme.appTypography.btnSemiBold18_h24.copy(
            color = MaterialTheme.extendedColors.unableContent,
        ),
        btnHeight = 50,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLine = maxLines,
        overFlowSetting = TextOverflow.Ellipsis,
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "정보 확인 - 카테고리 / 프롬프트 선택")
@Composable
private fun ReviewContent_CategoryWithPromptPreview() {
    TeumTeumEatTheme {
        ReviewContent(
            speechBubbleText = "모든 설정이 끝났어요.\n지금 바로 시작해 볼까요?",
            hintText = "입력한 정보는 마이페이지에서 수정할 수 있어요",
            subjectLabel = "관심분야",
            subjectText = "IT > 앱개발 > Android",
            difficultyText = "중",
            promptText = "출퇴근길에 가볍게 풀 수 있게 만들어주세요.",
            studyPeriodText = "2주",
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "정보 확인 - 문서 / 프롬프트 미선택")
@Composable
private fun ReviewContent_DocumentNoPromptPreview() {
    TeumTeumEatTheme {
        ReviewContent(
            speechBubbleText = "모든 설정이 끝났어요.\n지금 바로 시작해 볼까요?",
            hintText = "입력한 정보는 마이페이지에서 수정할 수 있어요",
            subjectLabel = "문서이름",
            subjectText = "학습자료_2024.pdf",
            difficultyText = "하",
            promptText = "미선택",
            studyPeriodText = "4주",
            onNext = {},
        )
    }
}
