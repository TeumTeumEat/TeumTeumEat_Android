/*
package com.teumteumeat.teumteumeat.ui.component.wheel_time_picker

import android.graphics.Color
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

data class PickTimeFocusIndicator(
    val enabled: Boolean,
    val widthFull: Boolean = true,
    val background: Color = Color(),
    val shape: Shape = RectangleShape,
    val border: BorderStroke = BorderStroke(0.dp, Color())
)

@Immutable
class BorderStroke(val width: Dp, val brush: Brush) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BorderStroke) return false

        if (width != other.width) return false
        if (brush != other.brush) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width.hashCode()
        result = 31 * result + brush.hashCode()
        return result
    }

    override fun toString(): String {
        return "BorderStroke(width=$width, brush=$brush)"
    }

    fun copy(width: Dp = this.width, brush: Brush = this.brush): BorderStroke {
        return BorderStroke(width = width, brush = brush)
    }
}




*/
/** A shape describing the rectangle. *//*

val RectangleShape: androidx.compose.ui.graphics.Shape =
    object : androidx.compose.ui.graphics.Shape {
        override fun createOutline(
            size: androidx.compose.ui.geometry.Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline =
            Outline.Rectangle(size.toRect())

        override fun toString(): String = "RectangleShape"
    }*/
