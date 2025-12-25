package com.teumteumeat.teumteumeat.ui.component.category_pager

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.SelectableBaseOutlineButton
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.Category


private val previewCategoryTree = listOf(
    Category(
        id = "d1-1",
        name = "개발",
        children = listOf(
            Category(
                id = "d2-1",
                name = "안드로이드",
                children = listOf(
                    Category("d3-1", "Compose"),
                    Category("d3-2", "XML UI"),
                    Category(
                        "d3-3",
                        "아주아주아주 긴 Compose UI 기반 모바일 앱 개발 카테고리"
                    )
                )
            ),
            Category(
                id = "d2-2",
                name = "웹",
                children = listOf(
                    Category("d3-4", "React"),
                    Category("d3-5", "Next.js")
                )
            )
        )
    )
)
@OptIn(ExperimentalFoundationApi::class)
@Preview(
    name = "Category Depth Paging Full Flow",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
fun CategoryDepthPagingFullFlowPreview() {

    // 🔹 Preview 전용 상태 (ViewModel 역할)
    var depth1 by remember { mutableStateOf<Category?>(null) }
    var depth2 by remember { mutableStateOf<Category?>(null) }
    var depth3 by remember { mutableStateOf<Category?>(null) }

    // 🔹 Pager 상태
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { 3 }
    )

    // 🔹 현재 상태에 따른 목표 페이지 계산
    fun calculateTargetPage(): Int =
        when {
            depth1 == null -> 0
            depth2 == null -> 1
            else -> 2
        }

    // 🔹 상태 변경 시 Pager 자동 이동 (핵심)
    LaunchedEffect(depth1, depth2, depth3) {
        pagerState.animateScrollToPage(calculateTargetPage())
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ✅ 3뎁스 Breadcrumb
        CategoryBreadcrumb(
            depth1Name = depth1?.name,
            depth2Name = depth2?.name,
            depth3Name = depth3?.name,
            onClearDepth1 = {
                depth1 = null
                depth2 = null
                depth3 = null
            },
            onClearDepth2 = {
                depth2 = null
                depth3 = null
            },
            onClearDepth3 = {
                depth3 = null
            },
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Pager 영역 (Grid가 여기서 슬라이드됨)
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false, // ❗ 스와이프 금지
            modifier = Modifier.weight(1f)
        ) { page ->

            when (page) {

                // 1️⃣ Depth 1
                0 -> {
                    CategoryGrid(
                        categories = previewCategoryTree,
                        selectedId = depth1?.id,
                        onItemClick = { category ->
                            // 🔁 토글 지원
                            if (depth1?.id == category.id) {
                                depth1 = null
                                depth2 = null
                                depth3 = null
                            } else {
                                depth1 = category
                                depth2 = null
                                depth3 = null
                            }
                        }
                    )
                }

                // 2️⃣ Depth 2
                1 -> {
                    CategoryGrid(
                        categories = depth1?.children.orEmpty(),
                        selectedId = depth2?.id,
                        onItemClick = { category ->
                            if (depth2?.id == category.id) {
                                depth2 = null
                                depth3 = null
                            } else {
                                depth2 = category
                                depth3 = null
                            }
                        }
                    )
                }

                // 3️⃣ Depth 3
                2 -> {
                    CategoryGrid(
                        categories = depth2?.children.orEmpty(),
                        selectedId = depth3?.id,
                        onItemClick = { category ->
                            if (depth3?.id == category.id) {
                                depth3 = null
                            } else {
                                depth3 = category
                            }
                        }
                    )
                }
            }
        }

        // 🔍 디버깅용 상태 표시
        Text(
            text = buildString {
                append("선택 상태: ")
                append(depth1?.name ?: "—")
                append(" > ")
                append(depth2?.name ?: "—")
                append(" > ")
                append(depth3?.name ?: "—")
            },
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}




@Composable
fun CategoryGrid(
    categories: List<Category>,
    selectedId: String?,
    onItemClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = StaggeredGridCells.Fixed(2), // ✅ 정책 A: 2열 고정
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
        itemsIndexed(
            items = categories,
            key = { _, item -> item.id }
        ) { _, category ->
            SelectableBaseOutlineButton(
                text = category.name,
                isSelected = category.id == selectedId,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp),
                onClick = { onItemClick(category) }
            )
        }
    }
}



