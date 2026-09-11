package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.screen.d0_league.LeagueRankerUiModel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import java.nio.file.WatchEvent

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
                color = if (ranker.isMe) theme.backGroundYellow300 else Color.Transparent,
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${ranker.rank}",
            style = typo.bodyMedium16,
            color = theme.textGhost,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(24.dp)
                .height(19.dp)
        )

        Spacer(modifier= Modifier.width(12.dp))

        Text(
            text = ranker.nickname,
            style = typo.bodyMedium16,
        )

        Spacer(modifier= Modifier.weight(1f))


        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ){
            Text(
                text = "${ranker.snackCount}",
                style = typo.subtitleSemiBold20.copy(color = theme.textPointBlue),
            )
            Spacer(modifier= Modifier.width(2.dp))
            Text(
                text = "스낵",
                style = typo.bodyMedium16.copy(color = theme.textGhost),
            )
        }
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
