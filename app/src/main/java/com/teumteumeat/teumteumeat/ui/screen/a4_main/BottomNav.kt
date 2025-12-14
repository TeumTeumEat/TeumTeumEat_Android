package com.teumteumeat.teumteumeat.ui.screen.a4_main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun BottomNavigationBar(navController: NavController) {

    val items = listOf(
        BottomNavItem.AddingFile,
        BottomNavItem.Home,
        BottomNavItem.Library
    )

    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value?.destination?.route


    NavigationBar(
        modifier = Modifier.padding(bottom = 25.dp),
        containerColor = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->

                val isSelected = currentRoute == item.route
                val isHomeItem = item.route == BottomNavItem.Home.route

                CustomBottomNavItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            launchSingleTop = true       // 중복 생성 방지
                            restoreState = true          // 이전 상태 복원

                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true         // 상태 저장
                            }
                        }
                    }
                )
            }
        }

    }

}


