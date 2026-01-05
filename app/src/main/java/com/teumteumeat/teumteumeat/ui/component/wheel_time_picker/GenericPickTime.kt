package com.teumteumeat.teumteumeat.ui.component.wheel_time_picker

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anhaki.picktime.utils.PickTimeFocusIndicator
import com.anhaki.picktime.utils.PickTimeTextStyle
import com.teumteumeat.teumteumeat.utils.extendedColors

/**
 * A high-level composable that displays multiple wheels horizontally with a focus indicator.
 *
 * @param selectedTextStyle Text style for selected items inside the wheels.
 * @param verticalSpace Vertical space around the selected item in the focus indicator.
 * @param containerColor Background color of the entire picker container.
 * @param focusIndicator The style configuration for the focus indicator.
 * @param content Content to be displayed.
 */

@Composable
fun GenericPickTime(
    selectedTextStyle: PickTimeTextStyle,
    verticalSpace: Dp,
    focusIndicator: PickTimeFocusIndicator,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    var minContainerWidth by remember { mutableStateOf<Dp?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.extendedColors.buttonFillSecondary),
        contentAlignment = Alignment.Center
    ) {
        minContainerWidth?.let { width ->
            FocusIndicator(
                focusIndicator = focusIndicator,
                selectedTextStyle = selectedTextStyle,
                minWidth = width,
                verticalSpace = verticalSpace
            )
        }
        Row(
            modifier = Modifier
                .onGloballyPositioned {
                    minContainerWidth = with(density) { it.size.width.toDp() }
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * A composable that displays a focus indicator (such as a border and background)
 * to highlight the selected item in a wheel picker.
 *
 * @param focusIndicator Defines the appearance and behavior of the focus indicator (border, background, shape).
 * @param selectedTextStyle The text style used for the selected item (used to calculate height).
 * @param minWidth The minimum width of the indicator if widthFull is false.
 * @param verticalSpace Additional vertical spacing added to the indicator height.
 */

@Composable
fun FocusIndicator(
    focusIndicator: PickTimeFocusIndicator,
    selectedTextStyle: PickTimeTextStyle,
    minWidth: Dp,
    verticalSpace: Dp,
) {
    if (focusIndicator.enabled) {
        val density = LocalDensity.current

        val selectedTextLineHeightPx = measureTextHeight(selectedTextStyle)
        val selectedTextLineHeightDp = with(density) { selectedTextLineHeightPx.toDp() }

        var modifier = if (focusIndicator.border.width > 0.dp) Modifier.border(
            focusIndicator.border,
            focusIndicator.shape
        ) else Modifier

        modifier = modifier
            .clip(focusIndicator.shape)
            .background(focusIndicator.background)
            .padding(horizontal = 15.dp)
            .height(selectedTextLineHeightDp + verticalSpace)

        Box(
            modifier =
                if (focusIndicator.widthFull) {
                    modifier.fillMaxWidth()
                } else {
                    modifier.width(minWidth)
                }
        )
    }
}
