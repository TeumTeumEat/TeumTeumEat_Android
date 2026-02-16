package com.teumteumeat.teumteumeat.ui.component.modal


import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 모달 백그라운드(딤) + 바깥 클릭 처리용 오버레이
 */
@Composable
fun ModalOverlay(
    onOutsideClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000)) // dim
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onOutsideClick() }
    ) {
        // ⚠️ content 내부 클릭이 바깥 클릭으로 전파되지 않도록
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* consume */ }
        ) {
            content()
        }
    }
}

