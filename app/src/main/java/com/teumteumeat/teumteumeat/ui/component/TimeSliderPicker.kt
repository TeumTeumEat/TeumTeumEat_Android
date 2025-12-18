package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 40.dp,
    visibleCount: Int = 5
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex
    )

    // 스크롤 종료 시 중앙 아이템 계산
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex =
                listState.firstVisibleItemIndex + visibleCount / 2
            onSelected(centerIndex.coerceIn(items.indices))
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleCount),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                Text(
                    text = items[index],
                    fontSize = if (index == selectedIndex) 20.sp else 14.sp,
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == selectedIndex)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .height(itemHeight)
                        .wrapContentHeight()
                )
            }
        }
    }
}

@Composable
fun AmPmPicker(
    selected: String,
    onChange: (String) -> Unit
) {
    val items = listOf("오전", "오후")
    val selectedIndex = items.indexOf(selected)

    WheelPicker(
        items = items,
        selectedIndex = selectedIndex,
        onSelected = { onChange(items[it]) }
    )
}

@Composable
fun HourPicker(
    hour: Int,
    onChange: (Int) -> Unit
) {
    val items = (1..12).map { "${it}시" }

    WheelPicker(
        items = items,
        selectedIndex = hour - 1,
        onSelected = { onChange(it + 1) }
    )
}

@Composable
fun MinutePicker(
    minute: Int,
    onChange: (Int) -> Unit
) {
    val items = (0..59).map { "%02d분".format(it) }

    WheelPicker(
        items = items,
        selectedIndex = minute,
        onSelected = onChange
    )
}

@Composable
fun CustomTimePicker(
    modifier: Modifier = Modifier
) {
    var amPm by remember { mutableStateOf("오전") }
    var hour by remember { mutableStateOf(10) }
    var minute by remember { mutableStateOf(0) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AmPmPicker(amPm) { amPm = it }
        HourPicker(hour) { hour = it }
        MinutePicker(minute) { minute = it }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeSliderPickerPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CustomTimePicker()
        }
    }
}

