package com.teumteumeat.teumteumeat.ui.screen.a4_main.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

import com.teumteumeat.teumteumeat.R
import kotlin.math.roundToInt


@Composable
fun ExpandableAddMenuOverlay(
    onAddDocument: () -> Unit,
    onAddCategory: () -> Unit,
    offset: Offset?,
    isExpanded: Boolean,
) {
    Log.d("바텀 Fav 아이템 디버깅", "isExpanded : ${isExpanded}, offset: $offset")
    if (offset == null) return

    val density = LocalDensity.current
    var menuHeightPx by remember { mutableStateOf(0) } // ⭐ 핵심

    // ⭐ 최종 위치 미세 조정값
    val finalAdjustPx = with(density) { -20.dp.toPx() }



    Box(modifier = Modifier
        .fillMaxSize()
        .zIndex(1f)
        .graphicsLayer { clip = false } // ✅ 중요
    ) {

        Box(
            modifier = Modifier
                .graphicsLayer { clip = false } // ✅ 중요
                .offset {
                    IntOffset(
                        // ✅ X: 메뉴를 버튼 중앙에 정렬
                        x = (offset.x - with(density) { 28.dp.toPx() }).roundToInt(),

                        // ⭐ Y: 메뉴 하단이 + 버튼 상단에 오도록
                        y = (offset.y - menuHeightPx + finalAdjustPx).roundToInt()
                    )
                }
        ) {
            ExpandableAddMenu(
                isExpanded = isExpanded,
                onAddDocument = onAddDocument,
                onAddCategory = onAddCategory,
                onMeasured = { heightPx ->
                    menuHeightPx = heightPx
                },
            )
        }
    }
}


@Composable
fun ExpandableAddMenu(
    isExpanded: Boolean,
    onAddDocument: () -> Unit,
    onAddCategory: () -> Unit,
    onMeasured: (Int) -> Unit,
) {

    AnimatedVisibility(
        visible = isExpanded,
        modifier = Modifier.graphicsLayer { clip = false }, // ✅ 중요
        // ⭐ "아래에서 튀어나오는 느낌" 제거
        // ⭐ 여기만 조절하면 됨
        enter = slideInVertically(
            initialOffsetY = { fullHeight ->
                // ⬇️ 값 키울수록 "더 아래에서" 시작
                fullHeight
            }
        ) + fadeIn(),

        exit = slideOutVertically(
            targetOffsetY = { fullHeight ->
                fullHeight
            }
        ) + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .graphicsLayer { clip = false }
                .onGloballyPositioned { coordinates ->
                    onMeasured(coordinates.size.height) // ⭐ 높이 전달
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            FloatingActionItem(
                iconRes = R.drawable.icon_search_category,
                onClick = onAddCategory
            )

            FloatingActionItem(
                iconRes = R.drawable.icon_upload_file,
                onClick = onAddDocument
            )
        }
    }
}
