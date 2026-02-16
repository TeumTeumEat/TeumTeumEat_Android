package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.domain.model.on_boarding.TimeState

enum class AmPm { AM, PM }


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> InfiniteScrollWheelPicker(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5,
    itemHeight: Dp = 40.dp,
    text: @Composable (T, Boolean) -> Unit
) {
    require(items.isNotEmpty())

    val middle = Int.MAX_VALUE / 2
    val startIndex = middle - (middle % items.size)

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = startIndex
    )

    val flingBehavior = rememberSnapFlingBehavior(listState)

    // ✅ 초기 선택값을 중앙에 맞춤
    LaunchedEffect(Unit) {
        val indexInItems = items.indexOf(selectedItem).coerceAtLeast(0)
        listState.scrollToItem(startIndex + indexInItems - visibleCount / 2)
    }

    // ✅ 스크롤 종료 시 중앙 아이템 계산
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex =
                listState.firstVisibleItemIndex + visibleCount / 2

            val realIndex = ((centerIndex % items.size) + items.size) % items.size
            onItemSelected(items[realIndex])
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleCount),
        contentAlignment = Alignment.Center
    ) {
        // 중앙 포커스 영역
        Box(
            modifier = Modifier
                .height(itemHeight)
                .fillMaxWidth()
                .background(
                    color = Color(0xFFF3F3F3),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight * (visibleCount / 2))
        ) {
            items(Int.MAX_VALUE) { index ->
                val item = items[index % items.size]
                val isSelected =
                    (listState.firstVisibleItemIndex + visibleCount / 2) == index

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    text(item, isSelected)
                }
            }
        }
    }
}

@Composable
fun WheelText(
    text: String,
    selected: Boolean
) {
    Text(
        text = text,
        fontSize = if (selected) 24.sp else 18.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) Color(0xFF222222) else Color(0xFF9F9F9F)
    )
}

@Composable
fun InfiniteTimePicker(
    initialTime: TimeState,
    onTimeChange: (TimeState) -> Unit
) {
    var state by remember { mutableStateOf(initialTime) }

    val hours = (1..12).toList()
    val minutes = (0..50 step 10).toList()
    val amPmList = listOf(AmPm.AM, AmPm.PM)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 시 (무한)
            InfiniteScrollWheelPicker(
                items = hours,
                selectedItem = state.hour,
                onItemSelected = {
                    state = state.copy(hour = it)
                    onTimeChange(state)
                }
            ) { value, selected ->
                WheelText(value.toString(), selected)
            }

            Spacer(Modifier.width(16.dp))

            // 분 (10분 단위, 무한)
            InfiniteScrollWheelPicker(
                items = minutes,
                selectedItem = state.minute,
                onItemSelected = {
                    state = state.copy(minute = it)
                    onTimeChange(state)
                }
            ) { value, selected ->
                WheelText("%02d".format(value), selected)
            }

            Spacer(Modifier.width(16.dp))

            // 오전 / 오후 (무한)
            InfiniteScrollWheelPicker(
                items = amPmList,
                selectedItem = state.amPm,
                onItemSelected = {
                    state = state.copy(amPm = it)
                    onTimeChange(state)
                }
            ) { value, selected ->
                WheelText(if (value == AmPm.AM) "오전" else "오후", selected)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfiniteTimePickerPreview() {
    var time by remember {
        mutableStateOf(
            TimeState(
                hour = 8,
                minute = 0,
                amPm = AmPm.AM
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InfiniteTimePicker(
            initialTime = time,
            onTimeChange = { time = it }
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "${time.amPm} ${time.hour}시 ${"%02d".format(time.minute)}분"
        )
    }
}


