package com.teumteumeat.teumteumeat.ui.component.Banner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

/**
 * 라이브러리 화면 상단 탭 아래에 표시되는 리그 진입 배너.
 * 검은 배경의 둥근 사각형 전체 폭 배너로, 클릭 시 리그 화면으로 이동시킨다.
 */
@Composable
fun LeagueEntryBanner(
    modifier: Modifier = Modifier,
    text: String = "리그에 진입해 1등에 도전해 보세요 👑",
    cornerRadius: Dp = 16.dp,
    onClick: () -> Unit,
) {
    val theme = MaterialTheme.extendedColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            // iconBlack이 Black100과 동일한 값이라 검은 배경 용도로 재사용
            .background(color = theme.iconBlack)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.appTypography.bodyMedium14.copy(color = theme.textOnPrimary),
        )

        Icon(
            painter = painterResource(id = R.drawable.icon_arrow_right),
            contentDescription = "리그 진입 버튼",
            tint = theme.btnGray400,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLeagueEntryBanner() {
    TeumTeumEatTheme {
        LeagueEntryBanner(
            modifier = Modifier.padding(16.dp),
            onClick = {},
        )
    }
}
