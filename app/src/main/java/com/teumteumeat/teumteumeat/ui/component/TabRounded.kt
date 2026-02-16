package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

@Composable
fun RoundedTab(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = listOf("날짜별", "주제별")
    val materialTheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                materialTheme.secondaryContainer,
                RoundedCornerShape(24.dp),
            )
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (selectedIndex == index) materialTheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        indication = null, // ✅ 클릭 이펙트 제거
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (selectedIndex == index) materialTheme.onPrimary else materialTheme.surfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoundedTabPreview() {
    var selectedTab by remember { mutableStateOf(0) }

    TeumTeumEatTheme {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(all = 20.dp),
            contentAlignment = Alignment.Center // ✅ 화면 중앙 정렬
        ) {
            RoundedTab(
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        }
    }
}
