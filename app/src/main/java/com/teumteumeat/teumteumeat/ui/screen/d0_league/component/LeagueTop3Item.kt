package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.screen.d0_league.LeagueRankerUiModel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

// 카드 너비는 이 컴포넌트가 정하지 않는다 — 호출부(Row)에서 Modifier.weight(3f/4f/3f)로
// 2등:1등:3등 = 3:4:3 비율을 주고, 여기서는 그 폭을 그대로 채운다(fillMaxWidth). 이렇게 하면
// 화면 가용 폭 전체를 남김없이 나눠 쓰므로, 하드코딩된 dp 합이 화면 폭을 넘어 카드가 잘리는 문제가 없다.
// 각 카드는 가로:세로 = 10:8 비율을 유지한다.
private const val CardAspectRatio = 10f / 8f
private val CardCornerRadius = 16.dp

// 1등 마스코트/왕관 크기 — 마스코트 하단 일부가 카드 상단 테두리에 걸쳐 보인다.
private val FirstMascotSize = 40.dp
private val FirstMascotOverlap = 12.dp
private val FirstCrownSize = 28.dp
private val FirstCrownOverlapWithMascot = 6.dp

// 2·3등 왕관 크기 — 왕관 하단 [OtherCrownCardOverlap]만큼 카드 위에 겹쳐서(카드보다 위에) 그려진다.
private val OtherCrownSize = 32.dp
private val OtherCrownCardOverlap = 8.dp

/**
 * 리그 상위 3명(Top3)의 한 명을 표시하는 항목.
 * 카드 너비는 이 컴포넌트 자신이 아니라 호출부가 [modifier]로 넘기는 값(예: `Modifier.weight(3f)`)이
 * 정하며, 여기서는 그 폭을 그대로 채운다. 각 카드는 가로:세로 10:8 비율을 유지한다.
 * 1등만 마스코트 캐릭터가 카드 상단에 걸쳐 보인다.
 */
@Composable
fun LeagueTop3Item(
    modifier: Modifier = Modifier,
    ranker: LeagueRankerUiModel,
    isFirstPlace: Boolean = false,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography

    val crownIconRes = when (ranker.rank) {
        1 -> R.drawable.icon_crown_first
        2 -> R.drawable.icon_crown_second
        else -> R.drawable.icon_crown_third
    }
    // 카드 위로 튀어나오는 왕관(1등은 마스코트까지) 높이만큼 아이템 상단에 여백을 예약해,
    // 상위 안내 문구 등 형제 요소와 겹치지 않도록 한다.
    val topReserve = if (isFirstPlace) {
        (FirstMascotSize - FirstMascotOverlap) + (FirstCrownSize - FirstCrownOverlapWithMascot)
    } else {
        OtherCrownSize - OtherCrownCardOverlap
    }

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topReserve)
                .aspectRatio(CardAspectRatio)
                .border(1.dp, theme.btnGray200, RoundedCornerShape(CardCornerRadius))
                .background(color = theme.backgroundW100, shape = RoundedCornerShape(CardCornerRadius)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = ranker.nickname,
                style = if (isFirstPlace) typo.subtitleSemiBold24 else typo.subtitleSemiBold18,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            Spacer(Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${ranker.snackCount}",
                    style = if (isFirstPlace) typo.subtitleSemiBold24 else typo.subtitleSemiBold16,
                    color = theme.textPointBlue,
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "스낵",
                    style = if (isFirstPlace) typo.bodyMedium14.copy(
                        fontSize = 19.2.sp
                    )
                        else typo.bodyMedium14,
                    color = theme.textGhost,
                )
            }
        }

        if (isFirstPlace) {
            // 왕관은 아이템 맨 위(예약된 여백의 시작점)에 위치하고,
            // 마스코트는 왕관 바로 아래에서 시작해 카드 상단에 [FirstMascotOverlap]만큼 걸쳐진다.
            Image(
                painter = painterResource(id = crownIconRes),
                contentDescription = "${ranker.rank}등",
                modifier = Modifier
                    .size(FirstCrownSize)
                    .align(Alignment.TopCenter),
            )
            Image(
                painter = painterResource(id = R.drawable.character_front),
                contentDescription = null,
                modifier = Modifier
                    .size(FirstMascotSize)
                    .align(Alignment.TopCenter)
                    .offset(y = FirstCrownSize - FirstCrownOverlapWithMascot),
            )
        } else {
            Image(
                painter = painterResource(id = crownIconRes),
                contentDescription = "${ranker.rank}등",
                modifier = Modifier
                    .size(OtherCrownSize)
                    .align(Alignment.TopCenter),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLeagueTop3Item() {
    TeumTeumEatTheme {
        LeagueTop3Item(
            modifier = Modifier.width(120.dp),
            ranker = LeagueRankerUiModel(rank = 1, nickname = "특*잇", snackCount = 12),
            isFirstPlace = true,
        )
    }
}

// 실제 화면(LeagueScreen)과 동일하게 좌우 18dp만 남기고, 2등:1등:3등 = 3:4:3 비율로
// 가용 폭 전체를 채우는 모습을 재현한 프리뷰.
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewLeagueTop3Row() {
    TeumTeumEatTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            LeagueTop3Item(
                modifier = Modifier.weight(3f),
                ranker = LeagueRankerUiModel(rank = 2, nickname = "김*민", snackCount = 10),
            )
            LeagueTop3Item(
                modifier = Modifier.weight(4f),
                ranker = LeagueRankerUiModel(rank = 1, nickname = "틈*잇", snackCount = 12),
                isFirstPlace = true,
            )
            LeagueTop3Item(
                modifier = Modifier.weight(3f),
                ranker = LeagueRankerUiModel(rank = 3, nickname = "이*재", snackCount = 9),
            )
        }
    }
}
