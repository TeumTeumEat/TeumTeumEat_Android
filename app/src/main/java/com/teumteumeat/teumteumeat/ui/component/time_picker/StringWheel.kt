package com.teumteumeat.teumteumeat.ui.component.time_picker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.teumteumeat.teumteumeat.ui.component.time_picker.wheel.Wheel

@Composable
fun StringWheel(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    space: Dp,
    selectedTextStyle: PickTimeTextStyle,
    unselectedTextStyle: PickTimeTextStyle,
    extraRow: Int,
    isLooping: Boolean = false,
    overlayColor: Color,
) {
    Wheel(
        modifier = modifier,
        items = items,
        selectedItem = selectedIndex,
        onItemSelected = onItemSelected,
        space = space,
        selectedTextStyle = selectedTextStyle,
        unselectedTextStyle = unselectedTextStyle,
        extraRow = extraRow,
        isLooping = isLooping,
        overlayColor = overlayColor,
        itemToString = { it },
        longestText = items.maxBy { it.length }
    )
}
