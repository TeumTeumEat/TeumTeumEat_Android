package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResultTier
import com.teumteumeat.teumteumeat.ui.screen.d0_league.LeagueResultModalUi
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import java.time.LocalDate

// 트로피(1~10위) 모달 카드의 가로:세로 비율 (4:5). 세로가 남는 만큼 트로피 로티가 커진다.
private const val TROPHY_CARD_ASPECT_RATIO = 4f / 5f

// 트로피 받침 하단과 "n위" 텍스트 사이 간격
private val TROPHY_TO_RANK_GAP = 16.dp

// TODO: 2~10위·순위권 밖 문구는 시안에 없어 임시 문구 — 디자인/기획 확정 후 교체
private fun LeagueResultTier.message(): String = when (this) {
    LeagueResultTier.FIRST -> "이번주 간식왕은 바로 너!🍿👑\n누구보다 열심히 틈틈이 채웠네요."
    LeagueResultTier.SECOND -> "이번 주도 틈틈이 잘 먹었어요!\uD83C\uDF71\n" + "다음주엔 한입만 더하면 1위를 노릴지도?"
    LeagueResultTier.OUT_OF_RANK -> "이번주 순위권엔 진입하지 못했어요. \uD83D\uDE05\n" + "월요일, 다시 1등을 향해 화이팅!"
}

/**
 * 리그 종료 후 첫 진입 시 보여주는 결과 안내 모달.
 * 확인 버튼 없이 모달 바깥·카드 어디를 눌러도, 뒤로가기로도 닫히며 닫으면 [onDismiss]가 호출된다.
 * 단 '공유하기'를 누르면 [onShare]만 호출되고 모달은 유지된다.
 */
@Composable
fun LeagueResultDialog(
    modal: LeagueResultModalUi,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        LeagueResultCard(modal = modal, onDismiss = onDismiss, onShare = onShare)
    }
}

@Composable
private fun LeagueResultCard(
    modal: LeagueResultModalUi,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
) {
    // 트로피 구간은 카드 비율을 4:5 로 고정하고, 순위권 밖은 기존처럼 내용 높이를 따른다.
    val isTrophy = modal.tier != LeagueResultTier.OUT_OF_RANK

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 37.dp),
    ) {
        val sizeModifier = if (isTrophy) {
            Modifier.height(maxWidth / TROPHY_CARD_ASPECT_RATIO)
        } else {
            Modifier
        }

        // BaseModal 과 같은 카드 규격(20dp 라운드, modalShadow)
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(sizeModifier)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = MaterialTheme.extendedColors.modalShadow,
                    spotColor = MaterialTheme.extendedColors.modalShadow,
                )
                // 카드 어디를 눌러도 닫힌다(ripple 없음). '공유하기'는 자체 clickable 이 먼저 처리한다.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
        ) {
            Column(
                modifier = Modifier.padding(
                    vertical = 20.dp,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${modal.weekLabel} 리그 결과",
                    style = MaterialTheme.appTypography.titleBold22,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                if (isTrophy) {
                    // 카드 높이에서 다른 요소를 뺀 나머지를 트로피가 채우고, 받침 하단이 영역 맨 아래에 닿는다.
                    LeagueResultLottie(
                        tier = modal.tier,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )

                    Spacer(Modifier.height(TROPHY_TO_RANK_GAP))

                    Text(
                        text = "${modal.rank}위",
                        style = MaterialTheme.appTypography.titleBold22,
                    )

                    Spacer(Modifier.height(17.dp))
                } else {
                    // 순위권 밖에서는 순위를 표시하지 않는다.
                    LeagueResultLottie(
                        tier = modal.tier,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 280.dp),
                    )

                    Spacer(Modifier.height(8.dp))
                }

                Text(
                    text = modal.tier.message(),
                    style = MaterialTheme.appTypography.bodyMedium16_h22,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "공유하기",
                    style = MaterialTheme.appTypography.bodyMedium14,
                    color = MaterialTheme.extendedColors.textGhost,
                    modifier = Modifier
                        .clickable(onClick = onShare)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LeagueResultCardPreview() {
    TeumTeumEatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                LeagueResultCard(
                    modal = LeagueResultModalUi(LeagueResultTier.FIRST, 1, 12, "9월 5주차", LocalDate.of(2026, 9, 28)),
                    onDismiss = {},
                    onShare = {},
                )
            }
        }
    }
}
