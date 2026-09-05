package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.screen.d0_league.LeagueRankerUiModel
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

/**
 * 리그 상위 3명(포디움)의 한 명을 표시하는 항목.
 * 1등은 [isFirstPlace]로 표시해 더 크게 렌더링한다.
 */
@Composable
fun LeaguePodiumItem(
    modifier: Modifier = Modifier,
    ranker: LeagueRankerUiModel,
    isFirstPlace: Boolean = false,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography

    val medalEmoji = when (ranker.rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> ""
    }
    val avatarSize = if (isFirstPlace) 72.dp else 56.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = medalEmoji,
            fontSize = if (isFirstPlace) 28.sp else 22.sp,
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(avatarSize)
                .background(color = theme.unselectedContainer, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = theme.textGhost,
                modifier = Modifier.size(avatarSize * 0.6f),
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = ranker.nickname,
            style = typo.bodyMedium14Reg,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "${ranker.snackCount}스낵",
            style = typo.subtitleSemiBold16.copy(color = theme.textPointBlue),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLeaguePodiumItem() {
    TeumTeumEatTheme {
        LeaguePodiumItem(
            ranker = LeagueRankerUiModel(rank = 1, nickname = "특*잇", snackCount = 12),
            isFirstPlace = true,
        )
    }
}
