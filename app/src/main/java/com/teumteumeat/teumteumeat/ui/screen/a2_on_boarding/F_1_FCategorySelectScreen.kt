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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.category_pager.CategoryBreadcrumb
import com.teumteumeat.teumteumeat.ui.component.category_pager.CategoryGrid
import com.teumteumeat.teumteumeat.ui.theme.Typography
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
    uiState: UiStateOnBoardingMain,
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

    val mockAllCategories = listOf(

        // ======================
        // 1뎁스: 개발
        // ======================
        Category(
            id = "dev",
            name = "개발",
            children = listOf(

                // 2뎁스: 안드로이드
                Category(
                    id = "android",
                    name = "안드로이드",
                    children = listOf(
                        Category(
                            id = "android-compose",
                            name = "Jetpack Compose"
                        ),
                        Category(
                            id = "android-xml",
                            name = "XML UI"
                        ),
                        Category(
                            id = "android-arch",
                            name = "MVVM · Clean Architecture"
                        ),
                        Category(
                            id = "android-long",
                            name = "아주아주 긴 안드로이드 카테고리 이름으로 UI 깨짐 테스트"
                        )
                    )
                ),

                // 2뎁스: 웹
                Category(
                    id = "web",
                    name = "웹",
                    children = listOf(
                        Category(
                            id = "web-react",
                            name = "React"
                        ),
                        Category(
                            id = "web-next",
                            name = "Next.js"
                        ),
                        Category(
                            id = "web-frontend",
                            name = "프론트엔드 전반 (HTML / CSS / JS)"
                        )
                    )
                ),

                // 2뎁스: 백엔드
                Category(
                    id = "backend",
                    name = "백엔드",
                    children = listOf(
                        Category(
                            id = "backend-spring",
                            name = "Spring Boot"
                        ),
                        Category(
                            id = "backend-node",
                            name = "Node.js"
                        ),
                        Category(
                            id = "backend-msa",
                            name = "마이크로서비스 아키텍처"
                        )
                    )
                )
            )
        ),

        // ======================
        // 1뎁스: 디자인
        // ======================
        Category(
            id = "design",
            name = "디자인",
            children = listOf(
                Category(
                    id = "uiux",
                    name = "UI / UX 디자인",
                    children = listOf(
                        Category(
                            id = "figma",
                            name = "Figma"
                        ),
                        Category(
                            id = "design-system",
                            name = "디자인 시스템"
                        )
                    )
                )
            )
        ),

        // ======================
        // 1뎁스: 기획
        // ======================
        Category(
            id = "planning",
            name = "기획",
            children = listOf(
                Category(
                    id = "service-planning",
                    name = "서비스 기획",
                    children = listOf(
                        Category(
                            id = "user-flow",
                            name = "유저 플로우 설계"
                        ),
                        Category(
                            id = "wireframe",
                            name = "와이어프레임"
                        )
                    )
                )
            )
        ),

        // ======================
        // 1뎁스: 기타 (하위 없음 테스트)
        // ======================
        Category(
            id = "etc",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        Category(
            id = "etc1",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        Category(
            id = "etc2",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        Category(
            id = "etc3",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        Category(
            id = "etc4",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        Category(
            id = "etc5",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        Category(
            id = "etc6",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        Category(
            id = "etc7",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        Category(
            id = "etc8",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        Category(
            id = "etc9",
            name = "기타",
            children = emptyList() // ❗ 하위 없음 케이스
        ),

        )

    // todo. 페이지 로드 시 선택된 카테고리 데이터 가져오기 -> 백엔드에 Get 요청 구현
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
                    Spacer(modifier = Modifier.height(60.dp))

                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isSelectedDepth2Category) "어떤 분야에 관심이 있나요?" else "공부하고자 하는 분야를 선택하세요!",
                            style = Typography.headlineMedium.copy(
                                fontSize = 18.sp,
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Image(
                            painter = painterResource(R.drawable.character_front),
                            contentDescription = "앞을 보는 케릭터",
                            modifier = Modifier.size(width = 200.dp, height = 162.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(modifier = Modifier.height(25.dp))
                    }

                    // 3뎁스 카테고리 컴포넌트 구현
                    // ✅ Breadcrumb (3뎁스)
                    CategoryBreadcrumb(
                        depth1Name = selection.depth1?.name,
                        depth2Name = selection.depth2?.name,
                        depth3Name = selection.depth3?.name,
                        onClearDepth1 = { viewModel.clearDepth1() },
                        onClearDepth2 = { viewModel.clearDepth2() },
                        onClearDepth3 = { viewModel.clearDepth3() },
                        modifier = Modifier
                            .fillMaxWidth() // ✅ 전체 너비 사용
                    )

                    Spacer(modifier = Modifier.height(15.dp))

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

                                // 1뎁스
                                0 -> CategoryGrid(
                                    // categories = mockAllCategories,
                                    // todo. 실제 서버로부터 전체 카테고리 목록 응답 확인되면 UI 고도화 구현하기
                                    categories = uiState.categories,
                                    selectedId = selection.depth1?.id,
                                    onItemClick = viewModel::toggleDepth1,
                                    currentPage = page,
                                )

                                // 2뎁스
                                1 -> CategoryGrid(
                                    categories = selection.depth1?.children.orEmpty(),
                                    selectedId = selection.depth2?.id,
                                    onItemClick = viewModel::toggleDepth2,
                                    currentPage = page,
                                )

                                // 3뎁스
                                2 -> CategoryGrid(
                                    categories = selection.depth2?.children.orEmpty(),
                                    selectedId = selection.depth3?.id,
                                    onItemClick = viewModel::toggleDepth3,
                                    currentPage = page,
                                )
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
                        isEnabled = selection.depth3 != null,
                        onClick = {
                            // todo. viewModel 함수 구현 후 호출
                        },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}