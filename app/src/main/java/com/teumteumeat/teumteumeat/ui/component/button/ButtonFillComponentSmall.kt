package com.teumteumeat.teumteumeat.ui.component.button

import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography


@Composable
fun BaseFillSmallButton(
    modifier: Modifier = Modifier, // 추가: Modifier 적용 가능
    text: String = "",
    textStyle: TextStyle = MaterialTheme.appTypography.bodyMedium14_20,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {},
    conerRadius: Dp = 50.dp,
    minWidth: Dp = 49.dp,
    minHeight: Dp = 28.dp,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary


    Row(
        modifier = modifier
            .wrapContentWidth()
            .defaultMinSize(
                minWidth = minWidth,
                minHeight = minHeight,
            )
            .background(
                color = if (isEnabled)
                    primaryColor
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(conerRadius)
            )
            .clickable(
                enabled = isEnabled,
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal=12.dp,),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = textStyle,
            color = if (isEnabled) onPrimaryColor else Color.White
        )
    }
}


@Preview(showBackground = true)
@Composable
fun BaseFillSmallButtonPreview() {
    TeumTeumEatTheme {
        BaseFillSmallButton(
            text = "완료",
            onClick = {
                // Utils.UxUtils.moveActivity(context, LoginActivity::class.java, false)
            },
            conerRadius = 16.dp
        )
    }
}
