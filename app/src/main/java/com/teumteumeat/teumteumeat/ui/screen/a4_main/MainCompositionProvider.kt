package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.LeagueSpeechBubble
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.R
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
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.mutableLongStateOf
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.HomeViewModel
import com.teumteumeat.teumteumeat.ui.screen.common_screen.GoalLoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import kotlinx.coroutines.flow.collectLatest
import androidx.hilt.navigation.compose.hiltViewModel

private val DebugSkyBlue = Color(0xFF56CCF2)

// 프로세스 생존 기간 동안만 유지되는 플래그 — 앱 프로세스가 종료되었다가
// 새로 포그라운드 진입할 때만 리그 말풍선의 등장 애니메이션을 재생하기 위함.
// (프로세스가 살아있는 동안의 화면 회전·탭 이동 등에서는 다시 재생되지 않는다.)
private object LeagueBubbleEntranceState {
    var hasAnimatedThisProcess = false
}

@Composable
fun MainCompositionProvider(
    viewModel: MainViewModel,
    context: Context,
    activity: MainActivity,
) {
    val homeViewModel: HomeViewModel = hiltViewModel(activity)
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val homeScreenState by homeViewModel.screenState.collectAsStateWithLifecycle()

    val mainUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navHostController = rememberNavController()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val theme = MaterialTheme.extendedColors
    var isNavReady by remember { mutableStateOf(false) }

    // 🏆 리그 진입 말풍선 — 스트릭 표시 영역 하단 좌표(화면 루트 기준)
    var streakAnchor by remember { mutableStateOf<Offset?>(null) }
    // 말풍선을 실제로 배치할 Box 자신의 화면 루트 기준 원점
    // (Scaffold 등 상위 레이아웃이 암묵적으로 추가하는 오프셋과 무관하게, 항상 같은 좌표계에서 계산하기 위함)
    var overlayBoxOrigin by remember { mutableStateOf(Offset.Zero) }

    // 이 프로세스에서 처음 보여지는 경우에만 등장 애니메이션 재생
    val alreadyAnimatedLeagueBubble = remember { LeagueBubbleEntranceState.hasAnimatedThisProcess }
    var showLeagueBubbleAnimation by remember { mutableStateOf(alreadyAnimatedLeagueBubble) }

    LaunchedEffect(Unit) {
        if (!alreadyAnimatedLeagueBubble) {
            showLeagueBubbleAnimation = true
            LeagueBubbleEntranceState.hasAnimatedThisProcess = true
        }
    }

    val leagueBubbleScale by animateFloatAsState(
        targetValue = if (showLeagueBubbleAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "league_bubble_grow",
    )

    val sessionManager = viewModel.sessionManager // 세션메니저 정의

    // 🔥 전역 세션 이벤트 감지
    LaunchedEffect(Unit) {
        sessionManager.sessionEvent.collectLatest {
            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java, clearTask = true)
        }
    }

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
        LocalScreenState provides screenState,
    ) {

        val navBackStackEntry by navHostController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

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

        val lastBackPressedTime = remember { mutableLongStateOf(0L) }

        // ✅ 물리 뒤로가기 처리
        BackHandler {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastBackPressedTime.longValue <= 500L) {
                // ⏱ 0.5초 이내 두 번째 클릭 → 종료
                activity.finish()
            } else {
                lastBackPressedTime.longValue = currentTime
                Toast
                    .makeText(
                        context,
                        "한 번 더 누르면 앱이 종료됩니다",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }

        DefaultMonoBg(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                content = { padding ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { overlayBoxOrigin = it.positionInRoot() }
                    ) {
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
                                },
                                onStreakAnchorPositioned = { streakAnchor = it },
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                // 1️⃣ 실제 화면 콘텐츠
                                MainNavHost(
                                    modifier = Modifier.fillMaxSize(),
                                    navController = navHostController,
                                    startDestination = BottomNavItem.Home.route,
                                    paddingValue = padding,
                                    mainViewModel = viewModel,
                                )
                            }


                        }

                        // 🏆 리그 진입 말풍선 — 홈 탭에서만 표시(라이브러리 탭에서는 숨김).
                        // 꼬리 끝이 스트릭 숫자 중앙 위에 오도록 정렬하고,
                        // 스트릭 표시 하단 6dp 아래에 배치. 다른 콘텐츠 위·팝업류 아래(zIndex).
                        val anchor = streakAnchor
                        val isHomeTab = currentRoute == BottomNavItem.Home.route
                        if (anchor != null && leagueBubbleScale > 0f && isHomeTab) {
                            val density = LocalDensity.current
                            val gapPx = with(density) { 6.dp.toPx() }
                            val tailWidthDp = 14.dp
                            val tailHeightDp = 8.dp
                            val tailPaddingEndDp = 24.dp
                            val tailWidthPx = with(density) { tailWidthDp.toPx() }
                            val tailPaddingEndPx = with(density) { tailPaddingEndDp.toPx() }
                            // anchor는 화면 루트 기준 좌표이므로, 실제 배치되는 이 Box의 루트 기준
                            // 원점(overlayBoxOrigin)만큼 빼서 Box 로컬 좌표로 변환한다.
                            val targetTailTipX = anchor.x - overlayBoxOrigin.x
                            val targetTopY = anchor.y - overlayBoxOrigin.y + gapPx

                            LeagueSpeechBubble(
                                tailWidth = tailWidthDp,
                                tailHeight = tailHeightDp,
                                tailPaddingEnd = tailPaddingEndDp,
                                modifier = Modifier
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)
                                        // 꼬리 끝(tailTip)이 targetTailTipX(스트릭 숫자 중앙)에 오도록
                                        // 배치 x좌표를 말풍선의 실제 측정 너비 기준으로 역산한다.
                                        val x = (
                                            targetTailTipX + tailPaddingEndPx + tailWidthPx / 2f -
                                                placeable.width
                                            ).roundToInt()
                                        val y = targetTopY.roundToInt()
                                        layout(placeable.width, placeable.height) {
                                            placeable.placeRelative(x, y)
                                        }
                                    }
                                    .zIndex(1f)
                                    .graphicsLayer {
                                        scaleX = leagueBubbleScale
                                        scaleY = leagueBubbleScale
                                        // 꼬리가 붙어있는 지점을 피벗으로 삼아 아래로 자연스럽게 펼쳐지도록 설정
                                        val tailFractionX = if (size.width > 0f) {
                                            ((size.width - tailPaddingEndPx - tailWidthPx / 2f) / size.width)
                                                .coerceIn(0f, 1f)
                                        } else {
                                            0.8f
                                        }
                                        transformOrigin = TransformOrigin(tailFractionX, 0f)
                                        alpha = if (leagueBubbleScale > 0.3f) 1f else 0f
                                    },
                                onClick = {
                                    // TODO: 리그 화면 이동 연결
                                },
                            )
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

            // 🔹 요약글 생성 로딩 오버레이 (전체 화면을 덮음)
            if (homeScreenState is UiScreenState.Loading && homeUiState.processingState != null) {
                GoalLoadingScreen(
                    modifier = Modifier.fillMaxSize(),
                    title = homeUiState.loadingTitle,
                    message = homeUiState.loadingMessage,
                    progress = homeUiState.processingState?.progress
                )
            }

            // [DEBUG 전용] 테스트용 플로팅 버튼 묶음
            if (BuildConfig.DEBUG) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(bottom = 100.dp, start = 16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.alpha(0.5f),
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { homeViewModel.resetGoal() },
                            containerColor = DebugSkyBlue,
                            contentColor = Color.White,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(text = "목표 초기화")
                            },
                        )
                        ExtendedFloatingActionButton(
                            onClick = { homeViewModel.resetAdReward() },
                            containerColor = DebugSkyBlue,
                            contentColor = Color.White,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(text = "쿠폰 초기화")
                            },
                        )
                        ExtendedFloatingActionButton(
                            onClick = { homeViewModel.addTestQuizCount() },
                            containerColor = DebugSkyBlue,
                            contentColor = Color.White,
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                )
                            },
                            text = {
                                Text(text = "풀이 횟수 +1")
                            },
                        )
                    }
                }
            }

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
                        onAddDocument = { },
                        onAddCategory = { },
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