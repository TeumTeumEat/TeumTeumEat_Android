package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryActivity
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryArgs
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors


@Composable
fun HomeScreen(
    name: String,
    viewModel: HomeViewModel,
    uiState: UiStateHome,
    onTabOther: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current.applicationContext
    val activity = LocalActivityContext.current

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage
    val theme = MaterialTheme.extendedColors

    val snackState = uiState.snackState
    val canOpenSummary =
        snackState is SnackState.Available &&
                uiState.summaryQuery != null

    val query = uiState.summaryQuery

    /* ================= 이미지 리소스 결정 ================= */

    val backgroundRes = when (snackState) {
        is SnackState.Available ->
            R.drawable.main_back_eat_before

        else ->
            R.drawable.main_back_eat_after
    }

    val foodRes = when (snackState) {
        is SnackState.Available ->
            R.drawable.img_main_food

        else ->
            R.drawable.img_food_before
    }

    DefaultMonoBg(
        modifier = modifier,
        color = theme.backSurface,
        content = {

            // 상태별 이미지 표시 부분
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(backgroundRes),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 20.dp, y = (-20).dp),
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier.then(
                        if (canOpenSummary) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                Log.d("요약글 조회 디버깅", "요약글 조회${canOpenSummary}")
                                val intent = Intent(activity, SummaryActivity::class.java).apply {
                                    putExtra(SummaryArgs.KEY_GOAL_ID, query.goalId)
                                    putExtra(SummaryArgs.KEY_GOAL_TYPE, query.goalType.name)
                                    putExtra(SummaryArgs.KEY_DOCUMENT_ID, query.documentId)
                                    putExtra(SummaryArgs.KEY_CATEGORY_ID, query.categoryId)
                                }
                                activity.startActivity(intent)
                            }
                        } else {
                            Log.d("요약글 조회 디버깅", "요약글 조회${canOpenSummary}")
                            Modifier // 클릭 불가
                        }
                    )
                ) {
                    Image(
                        painterResource(foodRes),
                        contentDescription = "",
                        modifier = Modifier
                            .offset(
                                x = 10.dp,     // 👉 오른쪽으로 +30dp
                                y = (30).dp  // 👉 위로 -100dp
                            ),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                when (snackState) {

                    is SnackState.Available -> {
                        // 필요 시 안내 문구
                        Text(
                            "오늘의 냠냠지식이 \n" +
                                    "도착했어요!",
                            style = MaterialTheme.appTypography.titleBold22,
                            textAlign = TextAlign.Center
                        )
                    }

                    is SnackState.Consumed -> {
                        Text("오늘의 지식을\n" +
                                "다 먹었어요!",
                            style = MaterialTheme.appTypography.titleBold22,
                            textAlign = TextAlign.Center
                        )
                    }

                    is SnackState.Waiting -> {
                        Text("아직 학습 시간이 아니에요",
                            style = MaterialTheme.appTypography.titleBold22,
                            textAlign = TextAlign.Center,
                        )
                    }

                    is SnackState.Expired -> {
                        Text("목표 기간이 종료되었어요",
                            style = MaterialTheme.appTypography.titleBold22,
                            textAlign = TextAlign.Center,
                        )
                    }

                }

            }

        },

    )

}

/*
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

    // val fakeViewModel = remember { HomeViewModel() }
    TeumTeumEatTheme {
        HomeScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateHome(),
            onTabOther = {},
        )
    }
}*/
