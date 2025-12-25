package com.teumteumeat.teumteumeat.ui.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
    iconSize: Dp = 60.dp,
    iconRes: Int,
    onClick: () -> Unit,
) {

    val materialTheme = MaterialTheme.colorScheme

    val selectedColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        materialTheme.surfaceVariant
    }

    TeumTeumEatTheme {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFFFF),
                contentColor = materialTheme.surfaceVariant,
                disabledContainerColor = materialTheme.onSurfaceVariant,
                disabledContentColor = materialTheme.onSurfaceVariant
            ),
            border = BorderStroke(2.dp, selectedColor),
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
                    tint = selectedColor,
                    modifier = Modifier.size(iconSize)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = titleText,
                    style = Typography.titleLarge,
                    color = materialTheme.surfaceVariant,
                )

                Text(
                    text = labelText,
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
        if (!isSelectableContent) {
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
        } else {
            Column(
                modifier = modifier,
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
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


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun BoxButtonPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 50.dp),
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
