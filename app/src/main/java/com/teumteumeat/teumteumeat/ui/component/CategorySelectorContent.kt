package com.teumteumeat.teumteumeat.ui.component

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toDepth1CategoryLabel
import com.teumteumeat.teumteumeat.domain.model.on_boarding.toDepth2CategoryLabel
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.category_pager.CategoryGrid
import com.teumteumeat.teumteumeat.ui.component.modal.bubble.SpeechBubble
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.CategorySelectionState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors

/**
 * 카테고리 선택 UI — 온보딩과 목표 추가 화면이 공유하는 컴포넌트.
 *
 * @param speechBubbleText 상단 말풍선 텍스트 (화면마다 다름)
 * @param categories 1뎁스 카테고리 목록
 * @param selection 현재 뎁스별 선택 상태
 * @param targetCategoryPage ViewModel이 요청하는 이동 대상 페이지 인덱스
 * @param isLoading 카테고리 로딩 여부
 * @param pageErrorMessage 로딩 실패 에러 메시지 (null이면 에러 없음)
 * @param isCategorySelectionComplete 4뎁스까지 선택 완료 여부 (다음 버튼 활성화 조건)
 * @param loadKey 카테고리 최초 로드 트리거 키 (NavBackStackEntry.id 권장)
 * @param onLoadCategories 카테고리 로드/재시도 콜백
 * @param onNavigateBack 뎁스 뒤로 이동 콜백 (BackHandler용)
 * @param onToggleDepth1 1뎁스 항목 선택/해제 콜백
 * @param onToggleDepth2 2뎁스 항목 선택/해제 콜백
 * @param onToggleDepth3 3뎁스 항목 선택/해제 콜백
 * @param onToggleDepth4 4뎁스 항목 선택/해제 콜백
 * @param onNext "다음으로" 버튼 클릭 콜백
 */
@Composable
fun CategorySelectorContent(
    speechBubbleText: String,
    categories: List<Category>,
    selection: CategorySelectionState,
    targetCategoryPage: Int,
    isLoading: Boolean,
    pageErrorMessage: String?,
    isCategorySelectionComplete: Boolean,
    loadKey: String,
    onLoadCategories: () -> Unit,
    onNavigateBack: () -> Unit,
    onToggleDepth1: (Category) -> Unit,
    onToggleDepth2: (Category) -> Unit,
    onToggleDepth3: (Category) -> Unit,
    onToggleDepth4: (Category) -> Unit,
    onNext: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = targetCategoryPage,
        pageCount = { 4 }
    )

    BackHandler(enabled = pagerState.currentPage > 0) {
        onNavigateBack()
    }

    LaunchedEffect(loadKey) {
        onLoadCategories()
    }

    LaunchedEffect(targetCategoryPage) {
        pagerState.animateScrollToPage(targetCategoryPage)
    }

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
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SpeechBubble(text = speechBubbleText)
                        Image(
                            painter = painterResource(R.drawable.char_onboarding_five_one),
                            contentDescription = "앞을 보는 케릭터",
                            contentScale = ContentScale.Fit,
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    if (isLoading) {
                        LoadingScreen(
                            title = "분야를 불러오는 중입니다.",
                            message = "잠시만 기다려주세요.",
                            contentAlignment = Alignment.TopCenter,
                        )
                    } else if (pageErrorMessage != null) {
                        FullScreenErrorModal(
                            errorState = ErrorState(
                                title = "분야를 불러오지 못했어요",
                                description = pageErrorMessage,
                                retryLabel = "다시 시도하기",
                                onRetry = onLoadCategories,
                            ),
                            isShowBackBtn = false,
                        )
                    } else {
                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                            ) {
                                when (page) {
                                    0 -> CategoryGrid(
                                        categories = categories,
                                        selectedId = selection.depth1?.id,
                                        onItemClick = onToggleDepth1,
                                        verticalColumns = 2,
                                        currentPage = page,
                                        labelMapper = { it.name.toDepth1CategoryLabel() },
                                    )
                                    1 -> CategoryGrid(
                                        categories = selection.depth1?.children.orEmpty(),
                                        selectedId = selection.depth2?.id,
                                        onItemClick = onToggleDepth2,
                                        currentPage = page,
                                        wrapContentWidth = true,
                                        labelMapper = { it.name.toDepth2CategoryLabel() },
                                    )
                                    2 -> CategoryGrid(
                                        categories = selection.depth2?.children.orEmpty(),
                                        selectedId = selection.depth3?.id,
                                        onItemClick = onToggleDepth3,
                                        currentPage = page,
                                    )
                                    3 -> CategoryGrid(
                                        categories = selection.depth3?.children.orEmpty(),
                                        selectedId = selection.depth4?.id,
                                        onItemClick = onToggleDepth4,
                                        currentPage = page,
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface,
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
                            color = MaterialTheme.extendedColors.backgroundW100,
                        ),
                        isEnabled = isCategorySelectionComplete,
                        onClick = onNext,
                        conerRadius = 16.dp,
                    )
                }
            }
        },
    )
}

// ─── Preview helpers ───────────────────────────────────────────────────────────

private val previewDepth4 = Category(id = "android", name = "Android", serverCategoryId = 1)
private val previewDepth3 = Category(
    id = "mobile", name = "모바일",
    children = listOf(previewDepth4, Category("ios", "iOS", 2), Category("flutter", "Flutter", 3))
)
private val previewDepth2 = Category(
    id = "programming", name = "프로그래밍",
    children = listOf(previewDepth3, Category("web", "웹"), Category("server", "서버/백엔드"))
)
private val previewDepth1 = Category(
    id = "it", name = "IT/기술",
    children = listOf(previewDepth2, Category("data", "데이터 분석"), Category("ai", "AI/ML"))
)
private val previewCategories = listOf(
    previewDepth1,
    Category(id = "lang", name = "어학", children = listOf(Category("eng", "영어"), Category("jpn", "일본어"))),
    Category(id = "biz", name = "비즈니스"),
    Category(id = "law", name = "생활 법률 및 제도"),
    Category(id = "med", name = "의학/보건"),
    Category(id = "art", name = "예술/디자인"),
)

private val previewBubbleText = "어떤 주제에 관심 있으세요?\n맞춤 퀴즈를 준비해드릴게요"

private fun previewContent(
    selection: CategorySelectionState = CategorySelectionState(),
    targetPage: Int = 0,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    isComplete: Boolean = false,
): @Composable () -> Unit = {
    TeumTeumEatTheme {
        CategorySelectorContent(
            speechBubbleText = previewBubbleText,
            categories = previewCategories,
            selection = selection,
            targetCategoryPage = targetPage,
            isLoading = isLoading,
            pageErrorMessage = errorMessage,
            isCategorySelectionComplete = isComplete,
            loadKey = "preview",
            onLoadCategories = {},
            onNavigateBack = {},
            onToggleDepth1 = {},
            onToggleDepth2 = {},
            onToggleDepth3 = {},
            onToggleDepth4 = {},
            onNext = {},
        )
    }
}

@Preview(showBackground = true, name = "로딩 중")
@Composable
private fun CategorySelectorLoadingPreview() = previewContent(isLoading = true)()

@Preview(showBackground = true, name = "에러")
@Composable
private fun CategorySelectorErrorPreview() =
    previewContent(errorMessage = "네트워크 연결을 확인해주세요.")()

@Preview(showBackground = true, name = "1뎁스 — 미선택")
@Composable
private fun CategorySelectorDepth1Preview() = previewContent()()

@Preview(showBackground = true, name = "2뎁스 — IT/기술 선택 후")
@Composable
private fun CategorySelectorDepth2Preview() =
    previewContent(
        selection = CategorySelectionState(depth1 = previewDepth1),
        targetPage = 1,
    )()

@Preview(showBackground = true, name = "3뎁스 — 프로그래밍 선택 후")
@Composable
private fun CategorySelectorDepth3Preview() =
    previewContent(
        selection = CategorySelectionState(depth1 = previewDepth1, depth2 = previewDepth2),
        targetPage = 2,
    )()

@Preview(showBackground = true, name = "4뎁스 — 선택 완료")
@Composable
private fun CategorySelectorCompletePreview() =
    previewContent(
        selection = CategorySelectionState(
            depth1 = previewDepth1,
            depth2 = previewDepth2,
            depth3 = previewDepth3,
            depth4 = previewDepth4,
        ),
        targetPage = 3,
        isComplete = true,
    )()