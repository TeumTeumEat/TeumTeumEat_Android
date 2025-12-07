package com.teumteumeat.teumteumeat.ui.component

import android.view.ViewTreeObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp


@Composable
fun RoundedCornerColumn(
    modifier: Modifier,
    vertical: Arrangement.Vertical,
    horizontal: Alignment.Horizontal,
    bgColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = bgColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            ),
        verticalArrangement = vertical,
        horizontalAlignment = horizontal,
    ) {
        content() // ✅ 내부 UI를 추가할 수 있도록 설정
    }
}

@Composable
fun AllRoundedCornerColumn(
    bgColor: Color,
    verticalPadding: Int,
    horizontalPadding: Int,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding.dp)
            .background(
                color = bgColor,
                shape = RoundedCornerShape(
                    size = 16.dp
                )
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(verticalPadding.dp))
        content() // ✅ 내부 UI를 추가할 수 있도록 설정
        Spacer(modifier = Modifier.height(verticalPadding.dp))
    }
}


@Composable
fun DefaultMonoBg(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    color: Color,
    content: @Composable BoxScope.() -> Unit = {} // ✅ Box 내부에 Composable 추가 가능
) {
    val view = LocalView.current
    var isKeyboardOpen by remember { mutableStateOf(false) }

    // 🔹 키보드 상태 감지 (모든 Android 버전에서 동작)
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardOpen = keypadHeight > screenHeight * 0.15 // 키보드 높이가 15% 이상이면 열려 있다고 판단
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener) // 🔹 안전하게 리스너 해제
        }
    }

    val bottomPadding = if (isKeyboardOpen) 0.dp else innerPadding.calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = 0.dp,
                bottom = bottomPadding
            )
            .background(color = color),
    ) {
        content() // ✅ 내부에서 Composable UI를 받을 수 있도록 설정
    }
}

@Composable
fun DefaultGradientBg(
    innerPadding: PaddingValues = PaddingValues(0.dp),
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {} // ✅ Box 내부에 Composable 추가 가능
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 0.dp,
                bottom = innerPadding.calculateBottomPadding()
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        startColor,
                        endColor
                    )
                )
            )
    ) {
        content() // ✅ 내부에서 Composable UI를 받을 수 있도록 설정
    }
}

@Composable
fun ExamLoadingBackground(
    modifier: Modifier = Modifier,
    containerColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 88.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        RoundedCornerColumn(
            modifier = Modifier,
            vertical = Arrangement.Center,
            bgColor = containerColor,
            horizontal = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}
