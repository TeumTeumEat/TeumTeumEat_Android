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

    // ⭐ 확장 메뉴 컬럼 하단과 + 버튼 사이 간격 (16dp)
    // 메뉴가 열리면 홈 아이템이 80dp → 52dp로 줄어들며 + 버튼의 실제 렌더링 위치가
    // onPlusPositioned가 넘겨주는 값보다 약 16dp 아래로 이동하므로 추가 보정 포함
    val finalAdjustPx = with(density) { -48.dp.toPx() }

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
                        // ✅ X: 메뉴를 버튼 중앙에 정렬 (FloatingActionItem 크기의 절반)
                        x = (offset.x - with(density) { (FloatingActionItemSize / 2).toPx() }).roundToInt(),

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
                label = "주제 찾기",
                onClick = onAddCategory
            )

            FloatingActionItem(
                iconRes = R.drawable.icon_upload_file,
                label = "자료 올리기",
                onClick = onAddDocument
            )
        }
    }
}
