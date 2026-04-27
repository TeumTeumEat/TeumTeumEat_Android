package com.teumteumeat.teumteumeat.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.text.AutoSizeText
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography


@Composable
fun BaseOutlineButton(
    modifier: Modifier = Modifier,
    text: String = "",
    textStyle: TextStyle = TextStyle(),
    subText: String? = null,
    subTextStyle: TextStyle = TextStyle(),
    isEnabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    contentAligment: Alignment = Alignment.Center,
    btnHeight: Int = 50,
    maxLine: Int = 1,
    overFlowSetting: TextOverflow = TextOverflow.Clip,
) {
    val contentColor =
        if (isEnabled) color else MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor =
        if (isEnabled) color else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
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
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, outlineColor),
            modifier = Modifier
                .height(btnHeight.dp)
                .fillMaxWidth(),
        ) {
            if (subText != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = text,
                        style = textStyle,
                        maxLines = maxLine,
                        overflow = overFlowSetting,
                    )
                    Text(
                        text = subText,
                        style = subTextStyle,
                        maxLines = maxLine,
                        overflow = overFlowSetting,
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = contentAligment,
                ) {
                    Text(
                        text = text,
                        style = textStyle,
                        maxLines = maxLine,
                        overflow = overFlowSetting,
                    )
                }
            }
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
    onClick: () -> Unit,
    contentAligment: Arrangement.Horizontal = Arrangement.Center
) {
    val contentColor =
        if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor =
        if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = contentAligment,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 17.5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContainerColor = Color.White,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp), // border-radius: 8px
            border = BorderStroke(1.5.dp, outlineColor), // ✅ 버튼의 테두리 설정
            modifier = Modifier
                .defaultMinSize(minHeight = 60.dp)
                .fillMaxWidth(),
        ) {
            // ✅ 아이콘 (없으면 아예 안 그림)
            Row(
                modifier = Modifier.wrapContentHeight(), // Row가 버튼 안을 꽉 채우게 합니다.
                horizontalArrangement = Arrangement.Center, // 가로 중앙 정렬
                verticalAlignment = Alignment.CenterVertically // 세로 중앙 정렬 (매우 중요!)
            ) {
                CategoryIcon(text)

                AutoSizeText(
                    text = text,
                    modifier = Modifier.weight(1f, fill = false), // 버튼 너비를 넘지 않도록 설정
                    baseStyle = MaterialTheme.appTypography.btnSemiBold20_h24.copy(
                        color = contentColor,
                    )
                )

            }
        }
    }
}

@DrawableRes
fun getCategoryIconRes(categoryName: String): Int? {
    return when (categoryName) {
        "PM" -> R.drawable.icon_note
        "디자인" -> R.drawable.icon_design
        "웹 개발자", "웹개발자" -> R.drawable.icon_web
        "앱 개발자", "앱개발자" -> R.drawable.icon_app_dev
        "서버개발자", "서버 개발자" -> R.drawable.icon_server_dev
        else -> null
    }
}

@Composable
fun CategoryIcon(
    categoryName: String
) {
    val iconRes = getCategoryIconRes(categoryName) ?: return

    Image(
        painter = painterResource(id = iconRes),
        contentDescription = null
    )
    Spacer(modifier = Modifier.width(8.dp))
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
                },
            )
        }
    }
}
