package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.screen.d0_league.LeagueRankerUiModel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

/**
 * 리그 4위 이하 랭킹 리스트의 한 행. 내 랭킹([LeagueRankerUiModel.isMe])이면 배경으로 강조한다.
 */
@Composable
fun LeagueRankRow(
    modifier: Modifier = Modifier,
    ranker: LeagueRankerUiModel,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (ranker.isMe) theme.primaryContainer else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${ranker.rank}",
            style = typo.subtitleSemiBold16,
            color = theme.textSecondary,
            modifier = Modifier.width(28.dp),
        )

        Text(
            text = ranker.nickname,
            style = typo.bodyMedium16,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "${ranker.snackCount}스낵",
            style = typo.subtitleSemiBold16.copy(color = theme.textPointBlue),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLeagueRankRow() {
    TeumTeumEatTheme {
        LeagueRankRow(
            ranker = LeagueRankerUiModel(rank = 8, nickname = "이*민", snackCount = 5, isMe = true),
        )
    }
}
