package com.teumteumeat.teumteumeat.ui.screen.a4_main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

enum class LibraryTabType {
    DATE,   // 날짜별
    TOPIC   // 주제별
}

@Composable
fun LibraryTopTab(
    selectedTab: LibraryTabType,
    onTabSelected: (LibraryTabType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.extendedColors.backSurface)
            .padding(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        TabItem(
            modifier = Modifier.weight(1f),
            title = "날짜별",
            isSelected = selectedTab == LibraryTabType.DATE,
            onClick = { onTabSelected(LibraryTabType.DATE) }
        )

        TabItem(
            modifier = Modifier.weight(1f),
            title = "주제별",
            isSelected = selectedTab == LibraryTabType.TOPIC,
            onClick = { onTabSelected(LibraryTabType.TOPIC) }
        )
    }
}

@Composable
private fun TabItem(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val theme = MaterialTheme.extendedColors
    val typo = MaterialTheme.appTypography

    Column(
        modifier = modifier
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = typo.subtitleSemiBold16.copy(
                color = if (isSelected)
                    theme.primary
                else
                    theme.textGhost
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(theme.primary)
            )
        }
    }
}