package com.teumteumeat.teumteumeat.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme


@Composable
fun BaseButton(
    text: String = "",
    modifier: Modifier = Modifier, // 추가: Modifier 적용 가능
    isEnabled: Boolean = true,
    isColorReversed: Boolean = false,
    hasOutLine: Boolean = false,
    onClick: ()-> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val containerColor = if (isColorReversed) onPrimaryColor else primaryColor
    val contentColor = if (isColorReversed) primaryColor else onPrimaryColor
    val borderColor = if (hasOutLine) primaryColor else Color.Transparent

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onClick,
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp), // border-radius: 8px
            border = if(isEnabled) BorderStroke(1.5.dp, borderColor) else null, // ✅ 버튼의 테두리 설정
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight(600),
                    color = contentColor,
                    textAlign = TextAlign.Center,
                )
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview5() {
    TeumTeumEatTheme {
        BaseButton(text = "로그인",
            onClick = {
                // Utils.UxUtils.moveActivity(context, LoginActivity::class.java, false)
            }
        )
    }
}
