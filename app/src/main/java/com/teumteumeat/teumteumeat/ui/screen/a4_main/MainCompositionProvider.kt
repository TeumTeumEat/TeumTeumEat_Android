package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.UiStateHome
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.AddGoalActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.GoalRegisterArgs
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.ExpandableAddMenuOverlay
import com.teumteumeat.teumteumeat.ui.screen.c1_mypage.MyPageActivity
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalMainUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.extendedColors
import kotlinx.coroutines.flow.first
import kotlin.jvm.java

@Composable
fun MainCompositionProvider(
    viewModel: MainViewModel,
    context: Context,
    activity: MainActivity,
) {

    val mainUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navHostController = rememberNavController()
    val theme = MaterialTheme.extendedColors
    var isNavReady by remember { mutableStateOf(false) }

    LaunchedEffect(navHostController) {
        // ✅ NavHost가 graph를 세팅할 때까지 대기
        navHostController.currentBackStackEntryFlow.first()
        isNavReady = true
    }


    CompositionLocalProvider(
        LocalAppContext provides context,
        LocalActivityContext provides activity,
        LocalMainUiState provides mainUiState,
        LocalViewModelContext provides viewModel,
    ) {

        val navBackStackEntry by navHostController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route


        /*LaunchedEffect(mainUiState.currentScreenType) {
            when (mainUiState.currentScreenType) {
                MainScreenType.MAIN -> {
                    navHostController.navigate(BottomNavItem.Home.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navHostController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                }

                MainScreenType.LIBRARY -> {
                    navHostController.navigate(BottomNavItem.Library.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navHostController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                }
            }
        }*/

        LaunchedEffect(mainUiState.currentScreenType, isNavReady) {
            if (!isNavReady) return@LaunchedEffect

            val targetRoute = when (mainUiState.currentScreenType) {
                MainScreenType.MAIN -> BottomNavItem.Home.route
                MainScreenType.LIBRARY -> BottomNavItem.Library.route
            }

            // ✅ 이미 해당 화면이면 이동 안 함 (중복 방지)
            if (navHostController.currentDestination?.route == targetRoute) return@LaunchedEffect

            navHostController.navigate(targetRoute) {
                launchSingleTop = true
                restoreState = true

                // ⚠️ graph 직접 접근 ❌ → route 기반 popUpTo 사용
                popUpTo(BottomNavItem.Home.route) {
                    saveState = true
                }
            }
        }

        val extendedColors = MaterialTheme.extendedColors


        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
        ) {

            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                content = { padding ->

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = theme.backSurface)
                                .statusBarsPadding(),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {

                            MainTopBar(
                                currentStreak = mainUiState.currentStreak,
                                stampCount = mainUiState.stampCount,
                                onClickSetting = {
                                    Utils.UxUtils.moveActivity(
                                        context,
                                        MyPageActivity::class.java,
                                        exitFlag = false,
                                    )
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding()
                            ) {
                                // 1️⃣ 실제 화면 콘텐츠
                                MainNavHost(
                                    modifier = Modifier,
                                    navController = navHostController,
                                    startDestination = BottomNavItem.Home.route,
                                    paddingValue = padding,
                                )
                            }


                        }
                    }
                },
                bottomBar = {
                    BottomNavigationBar(
                        navHostController,
                        containerColor = Color.Transparent,
                        onClickPlus = {
                            viewModel.toggleBottomNavPlus()
                        },
                        onClosePlus = {
                            viewModel.closeBottomNavPlus()
                        },
                        isExpandedPlus = mainUiState.isExpandedBottomNavItemPlus,
                        onAddDocument = {
                            viewModel.closeBottomNavPlus()
                            // 문서목표 등록화면 이동
                        },
                        onAddCategory = {
                            viewModel.closeBottomNavPlus()
                            // 카테고리 목표 등록화면 이동
                        },
                        onPlusPositioned = viewModel::updatePlusButtonOffset,
                    )
                }
            )

            // 2️⃣ 반투명 배경
            if (mainUiState.isExpandedBottomNavItemPlus) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            viewModel.closeBottomNavPlus()
                        }
                )
            }


            // 🔹 플로팅 메뉴 (완전 오버레이)
            ExpandableAddMenuOverlay(
                onAddCategory = {
                    viewModel.closeBottomNavPlus()
                    activity.startActivity(
                        Intent(activity, AddGoalActivity::class.java).apply {
                            putExtra(
                                GoalRegisterArgs.KEY_GOAL_TYPE,
                                DomainGoalType.CATEGORY.name // ✅ String 전달
                            )
                        }
                    )
                },
                onAddDocument = {
                    viewModel.closeBottomNavPlus()
                    // 문서목표 등록화면 이동
                    activity.startActivity(
                        Intent(activity, AddGoalActivity::class.java).apply {
                            putExtra(
                                GoalRegisterArgs.KEY_GOAL_TYPE,
                                DomainGoalType.DOCUMENT.name // ✅ String 전달
                            )
                        }
                    )
                },
                offset = mainUiState.plusBtnOffset,
                isExpanded = mainUiState.isExpandedBottomNavItemPlus,
            )

        }

    }

}

@Preview(showBackground = true)
@Composable
fun HomeMainFramePreview() {

    val fakeViewModel = remember { UiStateHome() }
    val navHostController = rememberNavController()

    TeumTeumEatTheme {
        DefaultMonoBg(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    BottomNavigationBar(
                        navHostController,
                        containerColor = MaterialTheme.extendedColors.backSurface,
                        onClickPlus = {},
                        onClosePlus = {},
                        isExpandedPlus = false,
                        onAddDocument = {  },
                        onAddCategory = {  },
                        onPlusPositioned = { },
                    )
                },
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                            .padding(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        /**
                         * 홈화면 타이틀 바
                         */
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 19.dp, horizontal = 24.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {

                            Image(
                                painter = painterResource(R.drawable.logo_home),
                                contentDescription = "home logo",
                                contentScale = ContentScale.None
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.icon_fire_fill),
                                    contentDescription = "home logo",
                                    contentScale = ContentScale.None
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "0",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            IconButton(
                                onClick = {
                                },
                                modifier = Modifier.size(30.dp),

                                ) {
                                Icon(
                                    modifier = Modifier.padding(0.dp),
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "previous page"
                                )
                            }

                        }

//                        HomeNavHost(
//                            navController = navHostController,
//                            startDestination = BottomNavItem.Home.route,
//                            modifier = Modifier.padding(),
//                        )
                    }
                }
            )
        }
    }
}