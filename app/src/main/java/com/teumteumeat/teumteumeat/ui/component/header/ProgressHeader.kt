package com.teumteumeat.teumteumeat.ui.component.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.CustomProgressBar
import com.teumteumeat.teumteumeat.ui.component.SizeAnimationInvisible
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.appTypography

@Composable
fun ProgressHeader(
    modifier: Modifier = Modifier,
    currentStep: Int,   // 1-based
    totalSteps: Int,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // 🔙 뒤로가기 (첫 페이지에서는 비활성)
        SizeAnimationInvisible(
            isVisible = currentStep > 1,
            clickEnabled = currentStep > 1,
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_keboard_arrow_left),
                contentDescription = "이전 페이지",
                modifier = Modifier.clickable(
                    interactionSource = Utils.UiUtils.noRipple(),
                    indication = null,
                    onClick = onBackClick,
                ),
            )
        }

        // 📊 프로그레스바
        CustomProgressBar(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            currentStep = currentStep,
            totalSteps = totalSteps,
        )

        // 🔢 페이지 텍스트
        Text(
            text = "$currentStep / $totalSteps",
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.appTypography.captionRegular14
        )
    }
}

