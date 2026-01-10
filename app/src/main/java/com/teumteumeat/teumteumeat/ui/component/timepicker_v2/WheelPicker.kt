package com.teumteumeat.teumteumeat.ui.component.timepicker_v2

// Compose 기본
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope

// Layout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

// Arrangement / Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment

// LazyColumn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items

// Modifier & Input
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

// Animation / Fling
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.material3.MaterialTheme

// Draw / Graphics
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color.Companion.White

// Density / Unit
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// Layout 계산
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Constraints
import com.teumteumeat.teumteumeat.ui.theme.Blue10

// Coroutine
import kotlinx.coroutines.launch

// Kotlin
import kotlin.math.abs

// 🔽 프로젝트 커스텀 컬러 (실제 위치에 맞게 조정)
import com.teumteumeat.teumteumeat.ui.theme.Gray20
import com.teumteumeat.teumteumeat.utils.extendedColors


// Wheel Picker
@Composable
fun WheelPicker(
    modifier: Modifier = Modifier,
    items: List<String>,
    initialItem: String,
    onItemSelected: (Int, String) -> Unit = { _, _ -> },
    content: @Composable ((String, Boolean) -> Unit)
) {
    val density = LocalDensity.current
    val scrollState = rememberLazyListState(0)
    var lastSelectedIndex by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val itemHeight = 36.dp
    val itemHeightPx = with(density) { itemHeight.toPx() }

    Column(modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    val centerY = size.height / 2f
                    val rectTop = centerY - (itemHeightPx / 2f)
                    val rectHeight = itemHeightPx
                    drawRoundRect(
                        color = Blue10,
                        cornerRadius = CornerRadius(8.dp.toPx()),
                        blendMode = BlendMode.Multiply,
                        topLeft = Offset(0f, rectTop),
                        size = Size(size.width, rectHeight)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val availableHeight = this.constraints.maxHeight.toFloat()
            val currentPickerHeightPx = if (availableHeight == Constraints.Infinity.toFloat()) {
                with(density) { 220.dp.toPx() }
            } else {
                availableHeight
            }

            LaunchedEffect(currentPickerHeightPx) {
                val targetIndex = items.indexOf(initialItem)
                val safeTargetIndex = if (targetIndex >= 0) targetIndex else 0

                lastSelectedIndex = safeTargetIndex
                scrollState.scrollToItem(safeTargetIndex)
            }

            val pickerHeightDp = with(density) { currentPickerHeightPx.toDp() }
            val fadeHeightDp =
                with(density) { ((currentPickerHeightPx - itemHeightPx) / 2f).toDp() }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pickerHeightDp),
                state = scrollState,
                flingBehavior = rememberSnapFlingBehavior(scrollState),
                contentPadding = PaddingValues(vertical = fadeHeightDp)
            ) {
                items(
                    count = items.size,
                    itemContent = { i ->
                        val item = items[i]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .pointerInput(i) {
                                    detectTapGestures(
                                        onTap = {
                                            coroutineScope.launch {
                                                scrollState.animateScrollToItem(i)
                                            }
                                        }
                                    )
                                }
                                .onGloballyPositioned { coordinates ->
                                    val y = (coordinates.positionInParent().y) + (itemHeightPx / 2f)
                                    val parentHalfHeight = (currentPickerHeightPx / 2f)
                                    val isCurrentlySelected = abs(parentHalfHeight - y) <= (itemHeightPx / 2f)

                                    if (isCurrentlySelected && lastSelectedIndex != i && item.isNotEmpty()) {
                                        onItemSelected(i, item)
                                        lastSelectedIndex = i
                                    }
                                },
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            content(item, lastSelectedIndex == i)
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fadeHeightDp)
                    .align(Alignment.TopCenter)
                    .drawWithContent {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(White, White.copy(alpha = 0f))
                            )
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fadeHeightDp)
                    .align(Alignment.BottomCenter)
                    .drawWithContent {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(White.copy(alpha = 0f), White)
                            )
                        )
                    }
            )
        }
    }
}