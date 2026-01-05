package com.teumteumeat.teumteumeat.ui.component.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.CustomProgressBar

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
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // 🔙 뒤로가기 (첫 페이지에서는 비활성)
        if (currentStep > 1) {
            IconButton(
                onClick = onBackClick,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "previous page"
                )
            }
        } else {
            // 레이아웃 균형용 더미 공간
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(20.dp)
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
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

