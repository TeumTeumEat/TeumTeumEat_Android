package com.teumteumeat.teumteumeat.ui.screen.a4_main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.CustomBottomNavItem

sealed class BottomNavItem(
    val route: String,
    val iconRes: Int,
    val label: String,
) {
    object Home : BottomNavItem(
        "home",
        R.drawable.icon_home,
        "home"
    )

    object AddingFile : BottomNavItem(
        "Adding_attached_file",
        R.drawable.icon_plus,
        "Adding_attached_file",
    )

    object Library : BottomNavItem(
        "Library",
        R.drawable.icon_library,
        "Library",
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    navController: NavController,
    containerColor: Color,
    onClickPlus: () -> Unit,
    onClosePlus: () -> Unit,
    isExpandedPlus: Boolean,
    onAddDocument: () -> Unit,
    onAddCategory: () -> Unit,
    onPlusPositioned: (Offset) -> Unit, // ⭐ 추가
) {

    val items = listOf(
        BottomNavItem.AddingFile,
        BottomNavItem.Home,
        BottomNavItem.Library
    )

    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value?.destination?.route



    /* ===============================
     * 2️⃣ 실제 NavigationBar (고정)
     * =============================== */
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 32.dp),
        containerColor = containerColor,
        tonalElevation = 0.dp, // ✅ 색 왜곡 제거 (핵심)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->

                val isPlusItem = item == BottomNavItem.AddingFile
                val isSelected = when {
                    // 1️⃣ + 버튼이 열려 있으면 → +만 선택
                    isExpandedPlus && isPlusItem -> true

                    // 2️⃣ + 버튼이 열려 있으면 → 나머지는 전부 선택 해제
                    isExpandedPlus && !isPlusItem -> false

                    // 3️⃣ + 버튼이 닫혀 있으면 → 기존 route 기준
                    else -> currentRoute == item.route
                }

                Box(
                    modifier = if (isPlusItem) {
                        Modifier.onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInRoot()
                            val size = coordinates.size

                            val centerX = position.x + size.width / 2f
                            val topY = position.y

                            onPlusPositioned(
                                Offset(centerX, topY)
                            )
                        }
                    } else {
                        Modifier
                    }
                ) {
                    CustomBottomNavItem(
                        modifier = Modifier,
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (isPlusItem) {
                                if (isExpandedPlus) {
                                    onClosePlus()
                                } else {
                                    onClickPlus()
                                }
                            } else {
                                // ✅ 다른 탭 클릭 시 + 메뉴 닫기
                                onClosePlus()
                                navController.navigate(item.route) {
                                    launchSingleTop = true       // 중복 생성 방지
                                    restoreState = true          // 이전 상태 복원

                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true         // 상태 저장
                                    }
                                }
                            }
                        }

                    )
                }
            }
        }

    }
}


