package com.teumteumeat.teumteumeat.ui.screen.common_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.button.FillSecondaryButton
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState

@Composable
fun FullScreenErrorModal(
    errorState: ErrorState,
    isShowBackBtn: Boolean = true,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {

        if (isShowBackBtn) {
            ErrorTopBar(
                onBack = onBack
            )
        }

        ErrorContent(
            title = errorState.title,
            description = errorState.description
        )

        ErrorBottomAction(
            primaryText = errorState.retryLabel,
            onPrimary = errorState.onRetry,
            secondaryText = errorState.secondaryLabel,
            onSecondary = errorState.onSecondaryAction
        )
    }
}

@Composable
fun ErrorTopBar(
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(16.dp),
        contentAlignment = (Alignment.TopStart)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "previous page",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun ErrorContent(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorBottomAction(
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        // 🔹 Secondary Action 존재 → 버튼 2개
        if (secondaryText != null && onSecondary != null) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ⬅️ Secondary
                FillSecondaryButton(
                    onClick = onSecondary,
                    text = secondaryText,
                    modifier = Modifier.weight(1f)
                )

                // ➡️ Primary
                BaseFillButton(
                    onClick = onPrimary,
                    text = primaryText,
                    modifier = Modifier.weight(1f)
                )
            }

        } else {
            // 🔹 단일 버튼
            BaseFillButton(
                onClick = onPrimary,
                text = primaryText,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}




