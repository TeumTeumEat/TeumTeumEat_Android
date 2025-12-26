package com.teumteumeat.teumteumeat.ui.component.mypage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.CheckToggleButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography


@Composable
fun MyPageRow(
    modifier: Modifier = Modifier,
    title: String,
    rightContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.appTypography.bodySemiBold18,
            color = Color.Black
        )

        rightContent?.invoke()
    }
}

@Composable
fun MyPageTitleRow(
    title: String,
) {
    MyPageRow(
        title = title
    )
}

@Composable
fun MyPageToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    MyPageRow(
        title = title,
        rightContent = {
            CheckToggleButton(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    )
}

@Composable
fun MyPageNavigateRow(
    title: String,
    onClick: () -> Unit,
) {
    MyPageRow(
        title = title,
        rightContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "전체 보기",
                    style = MaterialTheme.appTypography.lableMedium12_h14,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    )
}

@Preview(
    name = "MyPageScreen - Gallery",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
@Composable
fun MyPageScreenGalleryPreview() {
    TeumTeumEatTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // 🔹 Title Row
            MyPageTitleRow(title = "Title")

            Divider()

            // 🔹 Toggle OFF 상태
            MyPageToggleRow(
                title = "알림 받기",
                checked = false,
                onCheckedChange = {}
            )

            Divider()

            // 🔹 Toggle ON 상태
            MyPageToggleRow(
                title = "알림 받기",
                checked = true,
                onCheckedChange = {}
            )

            Divider()

            // 🔹 Navigate Row
            MyPageNavigateRow(
                title = "Title",
                onClick = {}
            )
        }
    }
}

