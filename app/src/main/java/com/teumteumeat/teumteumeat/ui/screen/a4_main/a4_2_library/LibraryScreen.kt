package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.domain.model.common.GoalType
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.card.CalendarDailyLearningCard
import com.teumteumeat.teumteumeat.ui.component.card.TopicCategoryCard
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_3_daily_summary_detail.DailySummaryActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.LibraryTabType
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.LibraryTopTab
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.MotivationCard
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.MotivationUiState
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.StampCountBadgeStateful
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.calendar.CalendarPager
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.calendar.CalendarUiState
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.mapStreakToMotivationUiState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.BottomFadeOverlay
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.Utils.DailySummaryArgs
import com.teumteumeat.teumteumeat.utils.extendedColors
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.jvm.java

@Composable
fun LibraryScreen(
    name: String,
    viewModel: LibraryViewModel,
    uiState: UiStateLibrary,
    onClickOtherTab: () -> Unit,
    innerPadding: PaddingValues,
) {

    val activity = LocalActivityContext.current as MainActivity
    val theme = MaterialTheme.extendedColors

    val sessionManager = viewModel.sessionManager // 세션메니저 정의

    // 🔥 전역 세션 이벤트 감지
    LaunchedEffect(Unit) {
        sessionManager.sessionEvent.collectLatest {
            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.extendedColors.backgroundW100)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            /** 🔹 날짜별 / 주제별 탭 */
            LibraryTopTab(
                selectedTab = uiState.selectedLibraryTab,
                onTabSelected = { viewModel.selectLibraryTab(it) }
            )

            when(uiState.selectedLibraryTab){
                LibraryTabType.DATE -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(theme.backgroundW100)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.height(32.dp))

                        MotivationCard(
                            uiState = mapStreakToMotivationUiState(
                                isStreakBroken = uiState.isStreakBroken,
                                streak = uiState.currentStreak
                            ),
                            modifier = Modifier,
                        )
                        Spacer(Modifier.height(20.dp))

                        // ✅ 스탬프 카운트 뱃지 (상단)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            StampCountBadgeStateful(
                                modifier = Modifier.weight(1f),
                                title = "총 스탬프",
                                count = uiState.stampCount,
                            )

                            Spacer(Modifier.width(12.dp))

                            StampCountBadgeStateful(
                                modifier = Modifier.weight(1f),
                                title = "이번달 도장",
                                count = uiState.monthStampCount,
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // 📅 캘린더
                        CalendarPager(
                            modifier = Modifier.fillMaxWidth(),
                            uiState = uiState.calendarUiState,
                            // ✅ 월 변경 시
                            onMonthChange = { yearMonth ->
                                viewModel.onCalendarMonthChanged(yearMonth)
                            },

                            // ✅ 날짜 클릭 시
                            onDateClick = { date ->
                                viewModel.onCalendarDateSelected(date)
                            }
                        )

                        uiState.calendarUiState.dailyLearningList.forEach { item ->
                            CalendarDailyLearningCard(
                                title = item.title,
                                description = item.summarySnippet,
                                dateText = item.lastStudiedAt.toLocalDate()
                                    .format(DateTimeFormatter.ofPattern("MM.dd")),
                                goalType = item.type,   // ✅ Domain → UI 그대로 전달
                                onClick = {
                                    val intent = Intent(
                                        activity,
                                        DailySummaryActivity::class.java
                                    ).apply {
                                        putExtra(
                                            DailySummaryArgs.KEY_ID,
                                            item.id
                                        )
                                        putExtra(
                                            DailySummaryArgs.KEY_TYPE,
                                            item.type.name   // ✅ enum → String
                                        )
                                        putExtra(
                                            DailySummaryArgs.KEY_DATE,
                                            item.lastStudiedAt.toLocalDate().toString()
                                        )
                                    }

                                    activity.startActivity(intent)
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        Spacer(Modifier.height(200.dp))
                        // 👆 하단 버튼 + 페이드에 가려지지 않도록 여유
                    }
                }
                LibraryTabType.TOPIC -> {
                    // 🔹 주제별 탭
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        // 1. 등록된 주제가 없는 경우 처리
                        if (uiState.categoryHistories.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxHeight() // LazyColumn 전체 높이를 채우기 위해 사용
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "등록된 주제가 없습니다.\n새로운 학습을 시작해보세요!",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textAlign = TextAlign.Center
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            uiState.categoryHistories.forEach { category ->

                                // ✅ 카테고리 단위 Section
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {

                                        // ✅ 주제 카드 (uiState 기반)
                                        TopicCategoryCard(
                                            title = category.categoryName,
                                            isSelected = uiState.selectedCategoryName == category.categoryName,
                                            onClick = {
                                                viewModel.onClickCategory(category.categoryName)
                                            }
                                        )

                                        // ✅ AnimatedVisibility를 사용하여 애니메이션 적용
                                        AnimatedVisibility(
                                            visible = uiState.selectedCategoryName == category.categoryName,
                                            // 나타날 때: 페이드 인 + 아래로 펼쳐짐
                                            enter = fadeIn(
                                                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                                            ) + expandVertically(
                                                expandFrom = Alignment.Top
                                            ),
                                            // 사라질 때: 페이드 아웃 + 위로 접힘
                                            exit = fadeOut(
                                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                            ) + shrinkVertically(
                                                shrinkTowards = Alignment.Top
                                            )
                                        ) {
                                            // ✅ 선택된 카테고리일 때만 학습 카드 표시

                                                Column {
                                                    Spacer(modifier = Modifier.height(12.dp))

                                                    category.histories.forEach { history ->
                                                        CalendarDailyLearningCard(
                                                            title = history.title,
                                                            description = history.description,
                                                            dateText = history.dateText,
                                                            goalType = history.goalType,
                                                            onClick = {
                                                                val intent = Intent(
                                                                    activity,
                                                                    DailySummaryActivity::class.java
                                                                ).apply {
                                                                    putExtra(
                                                                        DailySummaryArgs.KEY_ID,
                                                                        history.id
                                                                    )
                                                                    putExtra(
                                                                        DailySummaryArgs.KEY_TYPE,
                                                                        GoalType.CATEGORY.name
                                                                    )
                                                                    putExtra(
                                                                        DailySummaryArgs.KEY_DATE,
                                                                        history.date.toLocalDate().toString()
                                                                    )
                                                                }

                                                                activity.startActivity(intent)
                                                            }
                                                        )

                                                        Spacer(modifier = Modifier.height(12.dp))
                                                    }
                                                }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BottomFadeOverlay(
            Modifier
                .align(Alignment.BottomCenter)
        )
    }

}


@Composable
fun LibraryRoute(
    name: String,
    onClickOtherTab: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel<LibraryViewModel>()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreen(
        name = name,
        viewModel = viewModel,
        uiState = uiState,
        onClickOtherTab = onClickOtherTab,
        innerPadding = PaddingValues(0.dp),
    )
}

@Composable
fun LibraryMockScreen(
    name: String,
    uiState: UiStateLibrary,
    onClickOtherTab: () -> Unit,
) {
    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage
    val theme = MaterialTheme.extendedColors

    DefaultMonoBg(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.extendedColors.backgroundW100,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    /** 🔹 날짜별 / 주제별 탭 */
                    LibraryTopTab(
                        selectedTab = uiState.selectedLibraryTab,
                        onTabSelected = {
                            // ❌ Preview에서는 ViewModel 호출 금지
                            // no-op
                        }
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(theme.backgroundW100)
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.height(32.dp))

                        MotivationCard(
                            uiState = mapStreakToMotivationUiState(
                                isStreakBroken = uiState.isStreakBroken,
                                streak = uiState.currentStreak
                            ),
                            modifier = Modifier,
                        )

                        Spacer(Modifier.height(20.dp))

                        // ✅ 스탬프 카운트 뱃지 (상단)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            StampCountBadgeStateful(
                                modifier = Modifier.weight(1f),
                                title = "총 스탬프",
                                count = uiState.stampCount,
                            )

                            Spacer(Modifier.width(12.dp))

                            StampCountBadgeStateful(
                                modifier = Modifier.weight(1f),
                                title = "이번달 도장",
                                count = uiState.monthStampCount,
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // 📅 캘린더
                        CalendarPager(
                            modifier = Modifier.fillMaxWidth(),
                            uiState = uiState.calendarUiState,

                            // ❌ Preview에서는 ViewModel 호출 금지
                            onMonthChange = { /* no-op */ },

                            // ❌ Preview에서는 ViewModel 호출 금지
                            onDateClick = { /* no-op */ }
                        )

                        Spacer(Modifier.height(200.dp))
                        // 👆 하단 버튼 + 페이드에 가려지지 않도록 여유
                    }
                }

                BottomFadeOverlay(
                    Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    )
}

private fun previewUiStateLibrary() = UiStateLibrary(
    currentStreak = 3,
    stampCount = 12,
    monthStampCount = 5,
    calendarUiState = CalendarUiState(
        currentMonth = YearMonth.now(),
        solvedDates = setOf(
            LocalDate.now().minusDays(1),
            LocalDate.now().minusDays(3)
        )
    ),
    selectedLibraryTab = LibraryTabType.DATE,
    motivationUiState = MotivationUiState(
        streakCount = 3,
        title = "3일 연속 달성!",
        backgroundColor = Color(0xFFE0F2FE)
    )
)


@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {

    TeumTeumEatTheme {
        LibraryMockScreen(
            name = "Android",
            uiState = previewUiStateLibrary(),
            onClickOtherTab = {}
        )
    }
}