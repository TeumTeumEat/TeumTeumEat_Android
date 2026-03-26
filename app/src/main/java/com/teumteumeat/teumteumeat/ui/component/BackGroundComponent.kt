package com.teumteumeat.teumteumeat.ui.component

import android.provider.CalendarContract.Colors
import android.view.ViewTreeObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.utils.extendedColors


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
    color: Color = MaterialTheme.extendedColors.backSurface,
    // 💡 상태바 확장 레이어 색상 (기존 다크모드 설정값 유지)
    statusBarOverlayColor: Color = Color(0x80000000),
    extensionHeight: Dp = 5.dp,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
            // 내비게이션 바(하단) 패딩만 먼저 적용
            .navigationBarsPadding()
    ) {
        // 1. 상태바 확장 레이어 (최상단 고정)
        if (isDark) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 실제 시스템 상태바 영역 배경
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(statusBarOverlayColor)
                )
                // 시각적 확장 영역 배경
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(extensionHeight)
                        .background(statusBarOverlayColor)
                )
            }
        }

        // 2. 실제 콘텐츠 영역
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 콘텐츠는 상태바 아래에서 시작되도록 패딩 처리
                .statusBarsPadding()
                // 확장된 레이어 높이만큼 추가 패딩을 주어 콘텐츠가 가려지지 않게 함
                .padding(top = if (isDark) extensionHeight else 0.dp)
                .padding(innerPadding),
            content = content,
            contentAlignment = contentAlignment
        )
    }
}

@Composable
fun ExpandedStatusBarBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0x4D000000), // 상태바와 동일한 반투명 검정
    extensionHeight: Dp = 12.dp // 추가로 더 넓어 보이고 싶은 만큼의 높이
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 1. 실제 시스템 상태바 영역만큼 공간 차지
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(backgroundColor)
        )
        // 2. 시각적으로 더 넓어 보이게 추가하는 여백
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(extensionHeight)
                .background(backgroundColor)
        )
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
