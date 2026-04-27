package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.component.category_pager.CategoryGrid
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toDepth2CategoryLabel
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

private fun calculatePage(sel: CategorySelectionState): Int =
    when {
        sel.depth1 == null -> 0
        sel.depth2 == null -> 1
        else -> 2
    }

@Composable
fun CategorySelectScreen(
    name: String = "",
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnboardingState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    navBackStackEntry: NavBackStackEntry,
) {

    val context = LocalContext.current
    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    val selection = uiState.categorySelection
    val pagerState = rememberPagerState(
        initialPage = uiState.targetCategoryPage,
        pageCount = { 3 }
    )

    //  - 전체 상태 초기화
    // ✅ 핵심: backStackEntry를 key로 사용
    LaunchedEffect(navBackStackEntry.id) {
        viewModel.loadCategories()
    }

    LaunchedEffect(uiState.targetCategoryPage) {
        pagerState.animateScrollToPage(uiState.targetCategoryPage)
    }

    val categorySelection = uiState.categorySelection
    val isSelectedDepth2Category =
        categorySelection.depth2 != null && categorySelection.depth1 != null

    DefaultMonoBg(
        extensionHeight = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SpeechBubble(text = "어떤 주제에 관심 있으세요?\n맞춤 퀴즈를 준비해드릴게요")
                        Image(
                            painter = painterResource(R.drawable.char_onboarding_five_one),
                            contentDescription = "앞을 보는 케릭터",
                            contentScale = ContentScale.Fit,
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    if (uiState.isLoading) {
                        LoadingScreen(
                            title = "분야를 불러오는 중입니다.",
                            message = "잠시만 기다려주세요.",
                            contentAlignment = Alignment.TopCenter
                        )
                    } else if (uiState.pageErrorMessage != null) {
                        FullScreenErrorModal(
                            errorState = ErrorState(
                                title = "분야를 불러오지 못했어요",
                                description = uiState.pageErrorMessage,
                                retryLabel = "다시 시도하기",
                                onRetry = { viewModel.loadCategories() }
                            ),
                            isShowBackBtn = false
                        )
                    } else {

                        // ✅ Pager + Grid
                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp) // ✅ 각 페이지의 contentPadding
                            ) {
                                when (page) {

                                    // ⭐ 2뎁스 (기존 depth1.children)
                                    0 -> CategoryGrid(
                                        categories = uiState.categories
                                            .flatMap { it.children }, // depth1 skip
                                        selectedId = selection.depth2?.id,
                                        onItemClick = viewModel::toggleDepth2,
                                        currentPage = page,
                                        verticalColumns = 2,
                                        labelMapper = { it.name.toDepth2CategoryLabel() },
                                    )

                                    // ⭐ 3뎁스
                                    1 -> CategoryGrid(
                                        categories = selection.depth2?.children.orEmpty(),
                                        selectedId = selection.depth3?.id,
                                        onItemClick = viewModel::toggleDepth3,
                                        currentPage = page,
                                    )

                                    // ⭐ 4뎁스 (진짜 leaf)
                                    2 -> CategoryGrid(
                                        categories = selection.depth3?.children.orEmpty(),
                                        selectedId = selection.depth4?.id,
                                        onItemClick = viewModel::toggleDepth4,
                                        currentPage = page,
                                    )
                                }
                            }
                        }
                    }

                }

                // 2️⃣ 하단 그라데이션 (페이드 효과)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = MaterialTheme.appTypography.btnBold20_h24.copy(
                            color = MaterialTheme.extendedColors.backgroundW100
                        ),
                        isEnabled = uiState.isCategorySelectionComplete,
                        onClick = {
                            onNext()
                        },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}