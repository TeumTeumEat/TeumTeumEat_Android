package com.teumteumeat.teumteumeat.ui.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.ui.theme.Typography
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun BoxOutlineButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    titleText: String = "파일 업로드",
    lableText: String = "공부하고 싶은\n내용이 있어요.",
    iconRes: Int = R.drawable.icon_files,
    iconSize: Dp = 60.dp,
) {
    val materialTheme = MaterialTheme.colorScheme
    TeumTeumEatTheme {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF),
                contentColor = materialTheme.surfaceVariant,
                disabledContainerColor = materialTheme.onSurfaceVariant,
                disabledContentColor = materialTheme.onSurfaceVariant
            ),
            border = BorderStroke(2.dp, Color(0xFFDDDDDD)),
            onClick = onClick
        ) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = materialTheme.surfaceVariant,
                    modifier = Modifier.size(iconSize)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = titleText,
                    style = Typography.titleLarge,
                    color = materialTheme.surfaceVariant,
                )

                Text(
                    text = lableText,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    style = Typography.displayMedium.copy(
                    )
                )
            }
        }
    }
}

@Composable
fun SelectableBoxButton(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    titleText: String,
    labelText: String,
    iconRes: Int,
    onClick: () -> Unit,
) {

    val materialTheme = MaterialTheme.colorScheme
    val theme = MaterialTheme.extendedColors

    val contentColor = if (isSelected) { theme.primary } else { theme.textGhost }
    val borderColor = if (isSelected) { theme.primary } else { theme.btnLineDisable }

    TeumTeumEatTheme {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF),
                contentColor = materialTheme.surfaceVariant,
                disabledContainerColor = materialTheme.onSurfaceVariant,
                disabledContentColor = materialTheme.onSurfaceVariant
            ),
            border = BorderStroke(2.dp, borderColor),
            onClick = onClick
        ) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if(isSelected) MaterialTheme.extendedColors.primary
                        else MaterialTheme.extendedColors.btnGray200,
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = titleText,
                    style = MaterialTheme.appTypography.subtitleSemiBold20.copy(
                        color = contentColor
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = labelText,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    style = MaterialTheme.appTypography.bodyMedium14.copy(
                        color = contentColor
                    )
                )
            }
        }
    }
}


@Composable
fun ContentSelectableBoxButton(
    modifier: Modifier = Modifier,
    isSelectableContent: Boolean = false,
    titleText: String,
    lableText: String,
    contentFileName: String = "",
    onDelContentClick: () -> Unit,
    onClick: () -> Unit,
    iconRes: Int = R.drawable.icon_files,
    iconSize: Dp = 60.dp,

    ) {
    val materialTheme = MaterialTheme.colorScheme
    val theme = MaterialTheme.extendedColors
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF),
            contentColor = materialTheme.surfaceVariant,
            disabledContainerColor = materialTheme.onSurfaceVariant,
            disabledContentColor = materialTheme.onSurfaceVariant
        ),
        border = BorderStroke(2.dp, theme.btnLineDisable),
        onClick = onClick
    ) {
        if (!isSelectableContent) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.btnLineDisable,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = titleText,
                    style = MaterialTheme.appTypography.subtitleSemiBold20.copy(
                        color = if(isSelectableContent) MaterialTheme.extendedColors.unableContent
                            else MaterialTheme.extendedColors.textGhost
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                if(!isSelectableContent){
                    Text(
                        text = lableText,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        style = MaterialTheme.appTypography.bodyMedium14.copy(
                            color = MaterialTheme.extendedColors.textGhost
                        )
                    )
                }

            }
        } else {
            Box() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 13.dp, end = 19.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "파일 삭제 아이콘",
                        tint = materialTheme.onTertiary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = false, // 🔥 원형 리플
                                    radius = 16.dp,  // 🔥 리플 반지름
                                )
                            ) {
                                onDelContentClick()
                            }
                    )
                }
                Column(
                    modifier = modifier,
                ) {
                    Column(
                        modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = contentFileName,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            style = Typography.labelMedium.copy(
                                color = materialTheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }

    }
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun BoxButtonPreview() {
    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(all = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ContentSelectableBoxButton(
                isSelectableContent = true,
                contentFileName = "test file name.pdf",
                onClick = {},
                titleText = "Test",
                lableText = "test",
                onDelContentClick = {},
            )
            Spacer(modifier = Modifier.height(100.dp))
            BoxOutlineButton(
                onClick = {},
            )
        }
    }
}
