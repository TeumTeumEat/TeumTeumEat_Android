package com.teumteumeat.teumteumeat.ui.screen.common_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.GoalProgress
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    title: String = "",
    message: String = "",
    contentAlignment: Alignment = Alignment.Center,
    visibleStates: SnapshotStateList<Boolean> = remember {
        mutableStateListOf(false, false, false)
    },
) {
    val extendedColors = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.backgroundW100)
            .windowInsetsPadding(WindowInsets.systemBars) // ✅ SafeArea
    ) {

        Column(
            modifier = Modifier
                .align(contentAlignment)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 🔵 원형 컨테이너 (체크박스 UI 감성)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = extendedColors.primaryContainer,
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    color = extendedColors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔤 타이틀
            Text(
                text = title,
                style = typography.titleBold20,
                color = extendedColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔤 서브 타이틀
            Text(
                text = message,
                style = typography.bodyMedium14Reg,
                color = extendedColors.textGhost
            )

        }

    }

}


@Composable
fun GoalLoadingScreen(
    modifier: Modifier = Modifier,
    title: String = "",
    message: String = "",
    progress: Float? = null, // ⭐ 추가
    visibleStates: SnapshotStateList<Boolean> = remember {
        mutableStateListOf(false, false, false)
    },
    isCompletedLoading: Boolean = false
) {
    val extendedColors = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.backgroundW100)
            .windowInsetsPadding(WindowInsets.systemBars) // ✅ SafeArea
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ⭐ PROCESSING일 때만 커스텀 프로그레스 바 표시
            if (progress != null) {
                Spacer(modifier = Modifier.height(20.dp))

                GoalProgress(
                    progress = progress,
                    isCompletedLoading = isCompletedLoading
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔤 타이틀
            Text(
                text = title,
                style = typography.titleBold20,
                color = extendedColors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔤 서브 타이틀
            Text(
                text = message,
                style = typography.bodyMedium14Reg,
                color = extendedColors.textGhost
            )

        }

    }

}


@Composable
private fun LoadingCheckItem(
    text: String,
) {
    val colors = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = colors.primary,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.textOnPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = typography.bodyMedium16,
            color = colors.textPrimary
        )
    }
}