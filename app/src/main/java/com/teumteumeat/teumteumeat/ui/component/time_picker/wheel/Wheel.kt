package com.teumteumeat.teumteumeat.ui.component.time_picker.wheel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.component.time_picker.PickTimeTextStyle
import kotlinx.coroutines.launch

@Composable
internal fun <T> Wheel(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    space: Dp,
    selectedTextStyle: PickTimeTextStyle,
    unselectedTextStyle: PickTimeTextStyle,
    extraRow: Int,
    isLooping: Boolean,
    overlayColor: Color,
    itemToString: (T) -> String,
    longestText: String,
) {
    var localSelectedItem by remember { mutableIntStateOf(selectedItem) }

    val listState = if (isLooping) {
        rememberLazyListState(
            nearestIndexTarget(selectedItem - extraRow, items.size)
        )
    } else {
        rememberLazyListState(initialFirstVisibleItemIndex = selectedItem)
    }

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val spacePx = with(density) { space.toPx() }

    val selectedHeightPx = measureTextHeight(selectedTextStyle)
    val unselectedHeightPx = measureTextHeight(unselectedTextStyle)
    val widthPx = measureTextWidth(longestText, selectedTextStyle)

    val wheelHeight =
        with(density) {
            ((unselectedHeightPx.toDp() * (extraRow * 2)) +
                    (space * (extraRow * 2 + 2)) +
                    selectedHeightPx.toDp())
        }

    val maxOffset = unselectedHeightPx + spacePx

    val firstIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val offset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val isScrolling by remember { derivedStateOf { listState.isScrollInProgress } }

    val progress = (offset / maxOffset).coerceIn(0f, 1f)

    fun lerp(start: Float, end: Float) =
        start + (end - start) * progress


    LaunchedEffect(offset) {
        val index =
            (firstIndex + if (offset > maxOffset / 2) 1 else 0) % items.size
        localSelectedItem = index
        onItemSelected(index)
    }

    LaunchedEffect(isScrolling) {
        if (!isScrolling) {
            coroutineScope.launch {
                listState.animateScrollToItem(
                    firstIndex + if (offset > maxOffset / 2) 1 else 0
                )
            }
        }
    }

    Box(
        modifier = modifier
            .height(wheelHeight)
            .widthIn(min = with(density) { widthPx.toDp() })
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.Black,
                        0.25f to Color.Transparent
                    ),
                    blendMode = BlendMode.DstOut
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        0.75f to Color.Transparent,
                        1f to Color.Black
                    ),
                    blendMode = BlendMode.DstOut
                )
            },
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space),
            contentPadding = PaddingValues(vertical = space)
        ) {
            items(if (isLooping) Int.MAX_VALUE else items.size) { index ->
                val realIndex = index % items.size
                val isSelected = realIndex == localSelectedItem

                Text(
                    text = itemToString(items[realIndex]),
                    fontSize = lerp(
                        unselectedTextStyle.fontSize.value,
                        selectedTextStyle.fontSize.value
                    ).sp,
                    color = if (isSelected)
                        selectedTextStyle.color
                    else
                        unselectedTextStyle.color,
                    fontWeight = if (isSelected)
                        selectedTextStyle.fontWeight
                    else
                        unselectedTextStyle.fontWeight,
                    fontFamily = if (isSelected)
                        selectedTextStyle.fontFamily
                    else
                        unselectedTextStyle.fontFamily,
                                // ✅ 핵심 옵션
                    modifier = Modifier.wrapContentWidth(unbounded = true),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

private fun nearestIndexTarget(target: Int, size: Int): Int {
    val mid = Int.MAX_VALUE / 2
    return mid - (mid % size) + target
}
