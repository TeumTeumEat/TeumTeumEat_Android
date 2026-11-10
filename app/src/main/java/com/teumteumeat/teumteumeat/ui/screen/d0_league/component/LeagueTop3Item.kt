package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.screen.d0_league.LeagueRankerUiModel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import com.teumteumeat.teumteumeat.utils.toMaskedNickname
import kotlin.math.PI
import kotlin.math.acos

// 카드 너비는 이 컴포넌트가 정하지 않는다 — 호출부(Row)에서 Modifier.weight(3f/4f/3f)로
// 2등:1등:3등 = 3:4:3 비율을 주고, 여기서는 그 폭을 그대로 채운다(fillMaxWidth). 이렇게 하면
// 화면 가용 폭 전체를 남김없이 나눠 쓰므로, 하드코딩된 dp 합이 화면 폭을 넘어 카드가 잘리는 문제가 없다.
// 각 카드는 가로:세로 = 10:8 비율을 유지한다.
private const val CardAspectRatio = 10f / 8f
private val CardCornerRadius = 16.dp

/**
 * 1등 카드 로티 한 종류의 캔버스 비율과, 그 안에서 "league_card" 도형 레이어가 차지하는 상대 위치/크기.
 * 이 도형은 텍스트 레이어가 아니라 빈 카드 모양(흰 배경)이므로, 닉네임/스낵 텍스트와 테두리를
 * 이 비율로 계산한 위치에 Compose로 직접 오버레이해서 매핑한다.
 * 값은 league_card 도형의 위치(p)와 경로(v) 좌표를 그대로 계산한 것 — JSON이 바뀌면 다시 계산해야 한다.
 */
private class RankOneLottieSpec(
    @RawRes val rawRes: Int,
    val canvasAspectRatio: Float,
    val cardLeftFraction: Float,
    val cardTopFraction: Float,
    val cardWidthFraction: Float,
    val cardHeightFraction: Float,
)

// lottie_league_rank1_in_progress.json (실제 1등이 있을 때), 캔버스 123x153
//   position = [61.4946, 101.7934], path v 범위: x[-59.4, 59.4], y[-49.4, 49.4]
//   → x:[2.0946, 120.8946] (폭 118.8), y:[52.3934, 151.1934] (높이 98.8)
private val RankOneInProgressSpec = RankOneLottieSpec(
    rawRes = R.raw.lottie_league_rank1_in_progress,
    canvasAspectRatio = 123f / 153f,
    cardLeftFraction = 0.01703f,
    cardTopFraction = 0.34244f,
    cardWidthFraction = 0.96585f,
    cardHeightFraction = 0.64575f,
)

// lottie_league_rank1_unfilled.json (주간 랭킹 초기화 직후 1위 미등록), 캔버스 130x161
//   position = [61.9312, 109.6394], path v 범위: x[-59.4, 59.4], y[-49.4, 49.4]
//   → x:[2.5312, 121.3312] (폭 118.8), y:[60.2394, 159.0394] (높이 98.8)
private val RankOneUnfilledSpec = RankOneLottieSpec(
    rawRes = R.raw.lottie_league_rank1_unfilled,
    canvasAspectRatio = 130f / 161f,
    cardLeftFraction = 0.01947f,
    cardTopFraction = 0.37416f,
    cardWidthFraction = 0.91385f,
    cardHeightFraction = 0.61366f,
)

// 2·3등 왕관 크기 — 왕관 하단 [OtherCrownCardOverlap]만큼 카드 위에 겹쳐서(카드보다 위에) 그려진다.
private val OtherCrownSize = 32.dp
private val OtherCrownCardOverlap = 8.dp

// 실기기 스크린샷을 픽셀 단위로 측정해서 얻은, 카드 박스 폭 기준 마스코트(팔+얼굴)가 상단을 덮는 구간.
// 실측값은 약 0.118~0.893이었고, 여백을 살짝 더 둬서 캐릭터에 선이 닿지 않도록 0.10~0.90으로 잡았다.
// 로티 애니메이션이 바뀌면 이 값도 다시 측정해야 한다.
private const val TopGapStartFraction = 0.10f
private const val TopGapEndFraction = 0.90f

/**
 * 1등 카드 전용 테두리. 마스코트의 팔·입·코가 카드 상단 중앙 부분에 걸쳐 있어 일반 [Modifier.border]로
 * 사방을 다 그리면 그 위로 회색 선이 지나가 보인다. 그래서 상단 변 중 캐릭터가 겹치는 가운데 구간
 * ([TopGapStartFraction]~[TopGapEndFraction])만 비우고, 나머지는 전부 그린다.
 *
 * 실측한 겹침 경계(카드 폭의 10%~90%)가 상단 좌우 모서리 곡선(반지름 [cornerRadius]) 안쪽까지
 * 파고들 수 있어서, 그 경우 모서리를 통째로 그리면 곡선 끝부분이 캐릭터 위로 지나가 버린다.
 * 그래서 경계가 곡선 안쪽에 있으면 원의 방정식으로 정확한 각도를 구해 그 지점까지만 곡선을 그린다.
 */
private fun Modifier.cardBorderWithTopGap(
    width: Dp,
    color: Color,
    cornerRadius: Dp,
): Modifier = drawBehind {
    val strokeWidthPx = width.toPx()
    val r = cornerRadius.toPx()
    val w = size.width
    val h = size.height

    val gapStart = (w * TopGapStartFraction).coerceIn(0f, w)
    val gapEnd = (w * TopGapEndFraction).coerceIn(0f, w)
    val rightCenterX = w - r
    val leftCenterX = r

    // 겹침 구간 오른쪽 끝(gapEnd, 0)에서 시작해 시계 방향으로 한 바퀴 돌아
    // 겹침 구간 왼쪽 끝(gapStart, 0)에서 끝난다 — 그 사이(상단 가운데)만 안 그려진다.
    val path = Path().apply {
        if (gapEnd <= rightCenterX) {
            // 평평한 상단 변 위에 경계가 있음 — 그 지점에서 시작해 오른쪽으로 변을 그리고 우상단 모서리는 전부 그린다.
            moveTo(gapEnd, 0f)
            lineTo(rightCenterX, 0f)
            arcTo(Rect(w - 2 * r, 0f, w, 2 * r), startAngleDegrees = 270f, sweepAngleDegrees = 90f, forceMoveTo = false)
        } else {
            // 우상단 모서리 곡선 안쪽에 경계가 있음 — 곡선 중 캐릭터와 안 겹치지 않는 나머지(경계→360°)만 그린다.
            val cosTheta = ((gapEnd - rightCenterX) / r).coerceIn(-1f, 1f)
            val phi = (acos(cosTheta.toDouble()) * 180.0 / PI).toFloat()
            arcTo(Rect(w - 2 * r, 0f, w, 2 * r), startAngleDegrees = 360f - phi, sweepAngleDegrees = phi, forceMoveTo = true)
        }

        lineTo(w, h - r)
        arcTo(Rect(w - 2 * r, h - 2 * r, w, h), startAngleDegrees = 0f, sweepAngleDegrees = 90f, forceMoveTo = false)
        lineTo(r, h)
        arcTo(Rect(0f, h - 2 * r, 2 * r, h), startAngleDegrees = 90f, sweepAngleDegrees = 90f, forceMoveTo = false)
        lineTo(0f, r)

        if (gapStart >= leftCenterX) {
            // 평평한 상단 변 위에 경계가 있음 — 좌상단 모서리를 전부 그리고 그 지점까지 변을 그린다.
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), startAngleDegrees = 180f, sweepAngleDegrees = 90f, forceMoveTo = false)
            lineTo(gapStart, 0f)
        } else {
            // 좌상단 모서리 곡선 안쪽에 경계가 있음 — 곡선 중 캐릭터와 안 겹치지 않는 부분(180°→경계)만 그린다.
            val cosTheta = ((gapStart - leftCenterX) / r).coerceIn(-1f, 1f)
            val phi = (acos(cosTheta.toDouble()) * 180.0 / PI).toFloat()
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), startAngleDegrees = 180f, sweepAngleDegrees = 180f - phi, forceMoveTo = false)
        }
    }

    drawPath(path = path, color = color, style = Stroke(width = strokeWidthPx))
}

/**
 * 1등 로티 + 카드 영역 테두리 오버레이. [content]는 카드 도형 영역 안에 그려진다.
 * 로티 안의 카드 테두리는 카드 크기에 비례해 늘어나 화면마다 굵기가 달라지므로 제거했다.
 * 2·3등과 동일하게 항상 고정된 물리적 1dp 테두리를 여기서 직접 그리되, 마스코트의
 * 팔·입이 걸쳐 있는 상단은 [cardBorderWithTopGap]으로 비워 테두리가 그 위를 지나지 않게 한다.
 */
@Composable
private fun RankOneLottieCard(
    spec: RankOneLottieSpec,
    modifier: Modifier = Modifier,
    showCardBorder: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val theme = MaterialTheme.extendedColors
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(spec.rawRes))
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(spec.canvasAspectRatio),
    ) {
        LottieAnimation(
            composition = composition,
            progress = { lottieProgress },
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .offset(x = maxWidth * spec.cardLeftFraction, y = maxHeight * spec.cardTopFraction)
                .size(maxWidth * spec.cardWidthFraction, maxHeight * spec.cardHeightFraction)
                .then(
                    if (showCardBorder) Modifier.cardBorderWithTopGap(1.dp, theme.btnGray200, CardCornerRadius)
                    else Modifier,
                ),
            content = content,
        )
    }
}

/**
 * 카드에 표시할 닉네임. 서버 마스킹과 같은 규칙의 [toMaskedNickname]으로 표시 길이(최대 5자)를 보장한다.
 * 빈 자리([LeagueRankerUiModel.isEmpty])의 "-"는 마스킹 대상이 아니라 그대로 노출한다.
 */
private fun LeagueRankerUiModel.cardNickname(): String =
    if (isEmpty) nickname else nickname.toMaskedNickname()

/** 1등 카드 안에 표시하는 닉네임 + 스낵 수. */
@Composable
private fun RankOneCardContent(ranker: LeagueRankerUiModel) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = ranker.cardNickname(),
            style = typo.subtitleSemiBold24,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(2.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = ranker.snackCountText,
                style = typo.subtitleSemiBold24,
                color = theme.textPointBlue,
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = "스낵",
                style = typo.bodyMedium14.copy(fontSize = 19.2.sp),
                color = theme.textGhost,
            )
        }
    }
}

/**
 * 주간 랭킹이 초기화되어 아직 1위가 없을 때 1등 자리에 표시하는 카드.
 * 닉네임/스낵 수는 "-"로 표시하고, 회색 카드 테두리는 그리지 않는다.
 */
@Composable
fun LeagueFirstPlacePlaceholder(modifier: Modifier = Modifier) {
    RankOneLottieCard(modifier = modifier, spec = RankOneUnfilledSpec, showCardBorder = false) {
        RankOneCardContent(LeagueRankerUiModel.empty(rank = 1))
    }
}

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

    if (isFirstPlace) {
        // 로티 애니메이션(마스코트+왕관+카드) 자체에 텍스트 레이어가 없으므로, 카드 도형이
        // 차지하는 영역을 계산해 그 위에 닉네임/스낵 텍스트를 오버레이한다.
        RankOneLottieCard(modifier = modifier, spec = RankOneInProgressSpec) {
            RankOneCardContent(ranker)
        }
        return
    }

    val crownIconRes = if (ranker.rank == 2) R.drawable.icon_crown_second else R.drawable.icon_crown_third
    // 카드 위로 튀어나오는 왕관 높이만큼 아이템 상단에 여백을 예약해,
    // 상위 안내 문구 등 형제 요소와 겹치지 않도록 한다.
    val topReserve = OtherCrownSize - OtherCrownCardOverlap

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
                text = ranker.cardNickname(),
                style = typo.subtitleSemiBold18,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ranker.snackCountText,
                    style = typo.subtitleSemiBold16,
                    color = theme.textPointBlue,
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "스낵",
                    style = typo.bodyMedium14,
                    color = theme.textGhost,
                )
            }
        }

        Image(
            painter = painterResource(id = crownIconRes),
            contentDescription = "${ranker.rank}등",
            modifier = Modifier
                .size(OtherCrownSize)
                .align(Alignment.TopCenter),
        )
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
                ranker = LeagueRankerUiModel(rank = 1, nickname = "틈틈틈틈틈틈틈잇", snackCount = 12),
                isFirstPlace = true,
            )
            LeagueTop3Item(
                modifier = Modifier.weight(3f),
                ranker = LeagueRankerUiModel(rank = 3, nickname = "이*재", snackCount = 9),
            )
        }
    }
}
