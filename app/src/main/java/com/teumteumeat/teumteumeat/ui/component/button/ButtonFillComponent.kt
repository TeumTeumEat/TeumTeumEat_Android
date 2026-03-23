package com.teumteumeat.teumteumeat.ui.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun BaseFillButton(
    modifier: Modifier = Modifier, // 추가: Modifier 적용 가능
    text: String = "",
    textStyle: TextStyle = MaterialTheme.appTypography.btnBold20_h24,
    isEnabled: Boolean = true,
    isModalBtn: Boolean = false,
    onClick: () -> Unit,
    conerRadius: Dp = 16.dp,
    isLoading: Boolean = false,
    btnContainerColor: Color = MaterialTheme.colorScheme.primary,
    btnContentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val primaryColor = btnContainerColor
    val onPrimaryColor = btnContentColor

    val disableContainerColor =
        if (isModalBtn) MaterialTheme.extendedColors.btnGray200
        else MaterialTheme.colorScheme.surfaceVariant

    val disableContentColor =
        if (isModalBtn) MaterialTheme.extendedColors.textGhost else Color.White

    val contentColor = if (isEnabled) onPrimaryColor else disableContentColor

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            contentPadding = PaddingValues(0.dp),
            onClick = onClick,
            enabled = isEnabled && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = contentColor,
                disabledContainerColor = disableContainerColor,
                disabledContentColor = disableContentColor
            ),
            shape = RoundedCornerShape(conerRadius), // border-radius: 8px
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            // 1. 텍스트는 항상 그려서 버튼의 기준 높이를 잡아줍니다.
            // 단, 로딩 중일 때는 alpha를 0f로 주어 투명하게 만듭니다.
            Text(
                modifier = Modifier
                    .padding(vertical = 18.dp)
                    .alpha(if (isLoading) 0f else 1f), // 핵심 포인트!
                text = text,
                style = textStyle.copy(
                    color = contentColor
                ),
                textAlign = TextAlign.Center
            )

            // 2. 로딩 중일 때만 텍스트 위치(정중앙)에 인디케이터를 띄웁니다.
            if (isLoading) {
                CircularProgressIndicator(
                    // 이미 Text의 padding으로 높이가 확보되었으므로 인디케이터에는 size만 줍니다.
                    modifier = Modifier.size(15.dp),
                    color = MaterialTheme.extendedColors.textOnPrimary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BaseFillButtonPreview() {
    TeumTeumEatTheme {
        BaseFillButton(
            text = "로그인",
            onClick = {
                // Utils.UxUtils.moveActivity(context, LoginActivity::class.java, false)
            },
            conerRadius = 16.dp
        )
    }
}
