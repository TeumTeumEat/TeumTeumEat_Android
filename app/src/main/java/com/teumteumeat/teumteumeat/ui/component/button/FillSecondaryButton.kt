package com.teumteumeat.teumteumeat.ui.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun FillSecondaryButton(
    modifier: Modifier = Modifier, // 추가: Modifier 적용 가능
    text: String = "",
    textStyle: TextStyle = TextStyle(),
    isEnabled: Boolean = true,
    onClick: () -> Unit,
    conerRadius: Dp = 16.dp,
) {
    val primaryColor = MaterialTheme.extendedColors.primary
    val buttonFillSecondary = MaterialTheme.extendedColors.btnFillSecondary

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            contentPadding = PaddingValues(0.dp),
            onClick = onClick,
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonFillSecondary,
                contentColor = primaryColor,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(conerRadius), // border-radius: 8px
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                style = MaterialTheme.appTypography.btnMedium18_h24.copy(
                    textAlign = TextAlign.Center,
                    color = primaryColor
                ),
                modifier = Modifier.padding(vertical = 18.dp),
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FillSecondaryButtonPreview() {
    TeumTeumEatTheme {
        FillSecondaryButton(
            text = "로그인",
            onClick = {
                // Utils.UxUtils.moveActivity(context, LoginActivity::class.java, false)
            },
            conerRadius = 16.dp
        )
    }
}
