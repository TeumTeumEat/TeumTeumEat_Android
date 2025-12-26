package com.teumteumeat.teumteumeat.ui.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme


@Composable
fun BaseOutlineButton(
    modifier: Modifier = Modifier, // 추가: Modifier 적용 가능
    text: String = "",
    textStyle: TextStyle = TextStyle(),
    isEnabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val contentColor =
        if (isEnabled) color else MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor =
        if (isEnabled) color else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onClick,
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContainerColor = Color.White,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp), // border-radius: 8px
            border = BorderStroke(1.5.dp, outlineColor), // ✅ 버튼의 테두리 설정
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                style = textStyle
            )
        }
    }
}


@Composable
fun SelectableBaseOutlineButton(
    modifier: Modifier = Modifier, // 추가: Modifier 적용 가능
    text: String = "",
    textStyle: TextStyle = TextStyle(),
    isSelected: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val contentColor =
        if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor =
        if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContainerColor = Color.White,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp), // border-radius: 8px
            border = BorderStroke(1.5.dp, outlineColor), // ✅ 버튼의 테두리 설정
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                style = textStyle.copy(
                    color = contentColor
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BaseOutlineButtonPreview() {
    TeumTeumEatTheme {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            BaseOutlineButton(
                text = "button",
                isEnabled = true,
                onClick = {
                    // Utils.UxUtils.moveActivity(context, LoginActivity::class.java, false)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            SelectableBaseOutlineButton(
                text = "button",
                isSelected = false,
                onClick = {
                    // Utils.UxUtils.moveActivity(context, LoginActivity::class.java, false)
                }
            )
        }
    }
}
