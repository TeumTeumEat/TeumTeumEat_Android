package com.teumteumeat.teumteumeat.ui.component.button

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    textStyle: TextStyle = TextStyle(),
    isEnabled: Boolean = true,
    onClick: () -> Unit = {},
    conerRadius: Dp = 50.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = modifier.wrapContentWidth()
    ) {
        Button(
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.defaultMinSize(
                minWidth = 0.dp,
                minHeight = 0.dp
            ), // ✅ 최소 크기 제거
            onClick = onClick,
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = onPrimaryColor,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(conerRadius), // border-radius: 8px
        ) {
            Text(
                text = text,
                style = MaterialTheme.appTypography.bodyMedium14_20.copy(
                    color = onPrimaryColor,
                )
            )
        }
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
