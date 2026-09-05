package com.teumteumeat.teumteumeat.ui.screen.d0_league

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillSmallButton
import com.teumteumeat.teumteumeat.ui.component.button.NoPaddingIconButton
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.ui.screen.d0_league.component.LeaguePodiumItem
import com.teumteumeat.teumteumeat.ui.screen.d0_league.component.LeagueRankRow
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

// 랭킹 리스트 마지막 아이템과 풋터 사이의 최소 여백
private val RankListBottomSpacing = 8.dp

@Composable
fun LeagueScreen(
    uiState: UiStateLeague,
    screenState: UiScreenState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onInfoClick: () -> Unit,
    onRaiseRankClick: () -> Unit,
    onRetryApi: () -> Unit,
) {
    BackHandler {
        onBackClick()
    }

    when (screenState) {
        is UiScreenState.Error -> {
            FullScreenErrorModal(
                errorState = ErrorState(
                    title = "리그 정보를 불러오지 못했어요",
                    description = screenState.message,
                    retryLabel = "다시 시도하기",
                    onRetry = onRetryApi,
                ),
                onBack = onBackClick,
            )
        }

        UiScreenState.Loading, UiScreenState.Idle -> {
            LoadingScreen(
                title = "리그 정보를 불러오는 중",
                message = "잠시만 기다려주세요",
            )
        }

        UiScreenState.Success -> {
            LeagueContent(
                uiState = uiState,
                onBackClick = onBackClick,
                onShareClick = onShareClick,
                onInfoClick = onInfoClick,
                onRaiseRankClick = onRaiseRankClick,
            )
        }
    }
}

@Composable
private fun LeagueContent(
    uiState: UiStateLeague,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onInfoClick: () -> Unit,
    onRaiseRankClick: () -> Unit,
    // 톱바(고정)와 LeagueTitlePodiumSection(스크롤 콘텐츠 첫 아이템) 사이의 간격.
    // 필요에 따라 dp 값을 조정해서 사용한다.
    topBarToPodiumGap: Dp = 28.dp,
) {
    val density = LocalDensity.current
    val theme = MaterialTheme.extendedColors

    // 상단 아이콘 바 / 풋터 / 전체 컨테이너의 실제 렌더링 높이를 측정해 사용한다.
    // (하드코딩 대신 실측하므로 텍스트 줄바꿈 등으로 높이가 달라져도 겹치지 않는다)
    var topBarHeightPx by remember { mutableIntStateOf(0) }
    var footerHeightPx by remember { mutableIntStateOf(0) }
    var containerHeightPx by remember { mutableIntStateOf(0) }

    // 🎨 배경 그라데이션 렌더링 영역: 상태바 영역(y=0)부터 풋터 상단까지.
    // 컨테이너 전체 높이가 아니라 "풋터를 제외한" 높이를 기준으로 0%→100%가 정확히 끝나도록 한다.
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(theme.backGroundYellow200, theme.backgroundW100),
        startY = 0f,
        endY = (containerHeightPx - footerHeightPx).coerceAtLeast(1).toFloat(),
    )

    // 실측된 픽셀 높이를 Dp로 한 번에 변환 — 톱바 바로 아래(gap=0dp)에 콘텐츠가 딱 붙고,
    // topBarToPodiumGap을 더한 만큼만 벌어지도록 상단 여백을 계산한다.
    val (topBarHeightDp, footerHeightDp) = with(density) {
        topBarHeightPx.toDp() to footerHeightPx.toDp()
    }
    val listTopPadding = topBarHeightDp + topBarToPodiumGap
    val listBottomPadding = footerHeightDp + RankListBottomSpacing

    // ⚠️ 이 Box 자체는 스크롤되지 않는다 — 스크롤은 아래 LazyColumn 하나에서만 일어난다.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundBrush)
            .onGloballyPositioned { containerHeightPx = it.size.height },
    ) {
        // 🔹 "주간 리그 OPEN!" 타이틀, 1~3등 포디움, 4위 이하 랭킹 리스트가
        // 모두 하나의 스크롤 뷰(LazyColumn)에 속한다. 뒤로가기/공유/정보 아이콘 바만
        // 별도로 고정된다.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = listTopPadding,
                bottom = listBottomPadding,
            ),
        ) {
            item {
                LeagueTitlePodiumSection(uiState = uiState)
            }

            items(uiState.restRankers, key = { it.rank * 1_000 + it.nickname.hashCode() }) { ranker ->
                LeagueRankRow(
                    ranker = ranker,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }

        LeagueTopBar(
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            onInfoClick = onInfoClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .onGloballyPositioned { topBarHeightPx = it.size.height },
        )

        LeagueFooterSection(
            uiState = uiState,
            onRaiseRankClick = onRaiseRankClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .onGloballyPositioned { footerHeightPx = it.size.height },
        )
    }
}

/**
 * 리그 상단 헤더 (뒤로가기/공유/정보)
 * — 스크롤과 무관하게 항상 화면 상단에 고정된다.
 * 배경은 투명색, 스크롤되는 콘텐츠(타이틀/포디움)가 이 영역과 상태바 뒤로
 * 자연스럽게 비쳐 보이다가, 더 스크롤하면 화면(상태바) 위로 넘어가면 보이지 않게 된다.
 */
@Composable
private fun LeagueTopBar(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .statusBarsPadding()
            .padding(vertical = 16.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoPaddingIconButton(
            onClick = onBackClick,
            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
            contentDescription = "이전 화면",
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NoPaddingIconButton(
                onClick = onShareClick,
                imageVector = Icons.Rounded.Share,
                contentDescription = "리그 공유하기",
            )
            Spacer(modifier = Modifier.height(11.dp))
            NoPaddingIconButton(
                onClick = onInfoClick,
                imageVector = Icons.Rounded.Info,
                contentDescription = "리그 안내",
            )
        }
    }
}

/** 타이틀 + 안내 문구 + 1~3등 포디움 — 랭킹 리스트와 같은 스크롤 뷰의 첫 아이템으로 들어간다. */
@Composable
private fun LeagueTitlePodiumSection(
    uiState: UiStateLeague,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.appTypography
    val theme = MaterialTheme.extendedColors

    // 배경 그라데이션은 상위 컨테이너(LeagueContent)가 전체 스크롤 영역에 그려주므로
    // 여기서는 별도 배경 없이 투명하게 둔다.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Text(
            text = uiState.leagueTitle,
            style = typo.titleBold32.copy(color = theme.textSecondary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                append("다른 유저와 내 기록을 비교해요\n")
                append("매주 ")
                withStyle(SpanStyle(color = theme.textPointBlue)) {
                    append("최대 ${uiState.maxSnackCount}개")
                }
                append("의 스낵을 모을 수 있어요")
            },
            style = typo.bodyMedium14.copy(color = theme.textSecondary),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        // 🔹 포디움(상위 3명) — 2등, 1등, 3등 순서로 배치해 1등이 가운데 오도록 함
        val podium = uiState.podiumRankers
        val second = podium.getOrNull(1)
        val first = podium.getOrNull(0)
        val third = podium.getOrNull(2)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            second?.let {
                LeaguePodiumItem(ranker = it)
            }
            first?.let {
                LeaguePodiumItem(ranker = it, isFirstPlace = true)
            }
            third?.let {
                LeaguePodiumItem(ranker = it)
            }
        }
    }
}

@Composable
private fun LeagueFooterSection(
    uiState: UiStateLeague,
    onRaiseRankClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = MaterialTheme.appTypography
    val theme = MaterialTheme.extendedColors
    val myRanker = uiState.myRanker

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.backgroundBlack100)
            .navigationBarsPadding(),
    ) {
        Text(
            text = "리그 리셋까지 ${formatCountdown(uiState.resetRemainingSeconds)}",
            style = typo.captionRegular14.copy(color = Color.White),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        if (myRanker != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${myRanker.rank}",
                    style = typo.subtitleSemiBold16,
                    modifier = Modifier.size(28.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = myRanker.nickname, style = typo.subtitleSemiBold16)
                    Text(
                        text = "오늘 ${uiState.myTodaySnackCount}스낵 총 ${myRanker.snackCount}스낵",
                        style = typo.captionRegular12,
                    )
                }

                BaseFillSmallButton(
                    text = "순위 올리기",
                    onClick = onRaiseRankClick,
                )
            }
        }
    }
}

private fun formatCountdown(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun PreviewLeagueScreen() {
    TeumTeumEatTheme {
        LeagueScreen(
            uiState = UiStateLeague(
                rankers = listOf(
                    LeagueRankerUiModel(1, "틈*잇", 12),
                    LeagueRankerUiModel(2, "김*민", 10),
                    LeagueRankerUiModel(3, "이*재", 9),
                    LeagueRankerUiModel(4, "김*영", 8),
                    LeagueRankerUiModel(5, "임*현", 7),
                    LeagueRankerUiModel(5, "강*수", 7),
                    LeagueRankerUiModel(7, "이*", 6),
                    LeagueRankerUiModel(8, "이*민", 5, isMe = true),
                    LeagueRankerUiModel(9, "김*주", 4),
                    LeagueRankerUiModel(10, "박*훈", 4),
                    LeagueRankerUiModel(11, "최*아", 3),
                    LeagueRankerUiModel(12, "정*우", 3),
                    LeagueRankerUiModel(13, "한*빈", 3),
                    LeagueRankerUiModel(14, "오*진", 3),
                    LeagueRankerUiModel(15, "서*연", 2),
                    LeagueRankerUiModel(16, "황*서", 2),
                    LeagueRankerUiModel(17, "윤*호", 2),
                    LeagueRankerUiModel(18, "장*희", 2),
                    LeagueRankerUiModel(19, "신*아", 1),
                    LeagueRankerUiModel(20, "권*수", 1),
                ),
                myRanker = LeagueRankerUiModel(8, "이*민", 5, isMe = true),
                myTodaySnackCount = 1,
                resetRemainingSeconds = 23 * 3600L + 20 * 60L + 10L,
            ),
            screenState = UiScreenState.Success,
            onBackClick = {},
            onShareClick = {},
            onInfoClick = {},
            onRaiseRankClick = {},
            onRetryApi = {},
        )
    }
}
