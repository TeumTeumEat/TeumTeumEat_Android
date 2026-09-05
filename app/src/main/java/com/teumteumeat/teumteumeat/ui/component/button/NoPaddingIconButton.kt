package com.teumteumeat.teumteumeat.ui.component.button

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

private val NoPaddingIconButtonSize = 24.dp

/**
 * 기본 [IconButton]의 최소 터치 영역(48dp 내외) 패딩 없이,
 * 아이콘 크기(24dp)와 버튼 크기가 동일하게 맞춰진 아이콘 버튼.
 * 크기를 별도 파라미터로 받지 않고 항상 24dp로 고정된다.
 */
@Composable
fun NoPaddingIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(NoPaddingIconButtonSize),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(NoPaddingIconButtonSize),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNoPaddingIconButton() {
    TeumTeumEatTheme {
        NoPaddingIconButton(
            onClick = {},
            imageVector = Icons.Rounded.Info,
            contentDescription = "정보",
        )
    }
}
