package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

// TODO: 안내 문구는 디자인 시안 기준 — 기획 확정 후 변경될 수 있음
private val GuideBullets = listOf(
    "퀴즈를 풀면 점수가 쌓여 리그 순위가 올라가요.",
    "매주 새로운 리그가 시작돼요.",
    "틈틈잇 전체 사용자와 점수로 경쟁해요.",
)

/**
 * 리그 화면 헤더의 안내(i) 버튼을 눌렀을 때 보여주는 리그 소개 모달.
 * [onDismiss]는 '확인' 버튼·모달 바깥 터치·뒤로가기에서 호출된다.
 */
@Composable
fun LeagueGuideDialog(
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        LeagueGuideCard(onDismiss = onDismiss)
    }
}

@Composable
private fun LeagueGuideCard(
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.extendedColors

    // BaseModal / LeagueResultDialog 와 같은 카드 규격(20dp 라운드, modalShadow)
    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 37.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = colors.modalShadow,
                spotColor = colors.modalShadow,
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "리그에 도전해보세요!",
                style = MaterialTheme.appTypography.titleBold22,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.img_league_trophy_gold),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
            )

            Spacer(Modifier.height(16.dp))

            // 점수 규칙 요약 박스 — AdCouponModal 의 회색 안내 박스와 같은 규격
            Surface(
                color = colors.btnGray100,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.btnGray400,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "퀴즈 1회 = 1점\n점수 쌓을수록 → 주간 순위 UP",
                        style = MaterialTheme.appTypography.lableMedium12_h16,
                        color = colors.textPrimary,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GuideBullets.forEach { GuideBulletItem(text = it) }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                BaseFillButton(
                    text = "확인",
                    textStyle = MaterialTheme.appTypography.btnMedium18_h24,
                    isModalBtn = true,
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss,
                    conerRadius = 16.dp,
                )
            }
        }
    }
}

/** '•' 불렛 + 본문. 줄바꿈된 두 번째 줄이 본문 시작 위치에 맞도록 불렛과 본문을 분리해 배치한다. */
@Composable
private fun GuideBulletItem(text: String) {
    val style = MaterialTheme.appTypography.captionRegular12
    val color = MaterialTheme.extendedColors.textGhost

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "•", style = style, color = color)
        Spacer(Modifier.width(8.dp))
        Text(text = text, style = style, color = color)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LeagueGuideCardPreview() {
    TeumTeumEatTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            LeagueGuideCard(onDismiss = {})
        }
    }
}
