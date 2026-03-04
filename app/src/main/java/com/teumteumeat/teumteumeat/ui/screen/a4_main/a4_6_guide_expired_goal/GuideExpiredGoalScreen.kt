package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_6_guide_expired_goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.modal.BaseModal
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun GuideExpiredGoalScreen(
    onCloseClick: () -> Unit,
    onCreateNewGoalClick: (DomainGoalType) -> Unit,
) {

    // 1. 다이얼로그 표시 여부를 결정하는 로컬 상태 선언
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {

        // ❌ 닫기 버튼 (우측 상단)
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "닫기",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(40.dp)
                .clickable { onCloseClick() },
            tint = Color.Black
        )

        // 메인 컨텐츠
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            // 1. 로티 컴포지션 로드 (raw 폴더의 파일 지정)
            val composition by rememberLottieComposition(
                // ⚠️ 주의: 파일명 확장자(.json)는 뺍니다.
                spec = LottieCompositionSpec.RawRes(R.raw.quiz_comp)
            )

            // 2. 애니메이션 상태 제어 (반복 여부 등)
            val progress by animateLottieCompositionAsState(
                composition = composition,
                // 🔥 무한 반복 설정
                iterations = LottieConstants.IterateForever,
            )

            // 3. 화면에 그리기
            LottieAnimation(
                composition = composition,
                progress = { progress }, // 현재 애니메이션 진행 상태
                // contentScale = ContentScale.Fit, // 필요 시 스케일 설정
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 타이틀
            Text(
                text = "해당 주제를 \n모두 완주했어요!",
                style = MaterialTheme.appTypography.titleSemiBold32.copy(
                    textAlign = TextAlign.Center
                ),
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(23.dp))

            // 설명
            Text(
                text = "새로운 여정을 시작해 볼까요?",
                style = MaterialTheme.appTypography.subtitleSemiBold20,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }

        /** 🔹 하단 버튼 영역 */
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "홈으로 가기",
                    style = MaterialTheme.appTypography.subtitleSemiBold16
                        .copy(
                            color = MaterialTheme.extendedColors.textPointBlue
                        ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable(
                        onClick = onCloseClick
                    )
                )
            }

            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                BaseFillButton(
                    modifier = Modifier
                        .weight(1f),
                    onClick = {
                        showDialog = true
                    },
                    text = "새로운 틈틈잇 시작하기"
                )
            }
        }

        // 🔴 퇴장 확인 팝업 (가장 상단에 위치하도록 Box 마지막에 배치)
        if (showDialog) {
            // 배경을 어둡게 처리하기 위한 Box

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Dialog(
                    onDismissRequest = {
                        // 다이얼로그 바깥을 터치하거나 뒤로가기 버튼을 눌렀을 때 처리
                        showDialog = false
                    },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false // 커스텀 패딩을 적용하기 위해 기본 너비 제한 해제
                    )
                ) {
                    BaseModal(
                        title = "주제 형태를 선택하세요",
                        primaryButtonText = "카테고리 선택",
                        secondaryButtonText = "파일 업로드",
                        onPrimaryClick = {
                            onCreateNewGoalClick(DomainGoalType.CATEGORY)
                            showDialog = false
                         },
                        onSecondaryClick = {
                            onCreateNewGoalClick(DomainGoalType.DOCUMENT)
                            showDialog = false

                        }

                    )
                }
            }

        }
    }
}

@Preview(showBackground = true, name = "기한 만료 가이드 스크린")
@Composable
fun PreviewGuideExpiredGoalScreen() {
    // 프로젝트의 실제 테마가 있다면 아래와 같이 감싸주는 것이 좋습니다.
     TeumTeumEatTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            GuideExpiredGoalScreen(
                onCloseClick = { /* 미리보기이므로 동작 없음 */ },
                onCreateNewGoalClick = { },
            )
        }
     }
}
