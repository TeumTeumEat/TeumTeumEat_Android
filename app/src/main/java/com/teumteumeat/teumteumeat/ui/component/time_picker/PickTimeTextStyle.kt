package com.teumteumeat.teumteumeat.ui.component.time_picker

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

data class PickTimeTextStyle(
    val color: Color,
    val fontSize: TextUnit,
    val fontWeight: FontWeight,
    val fontFamily: FontFamily = FontFamily.Default
)
