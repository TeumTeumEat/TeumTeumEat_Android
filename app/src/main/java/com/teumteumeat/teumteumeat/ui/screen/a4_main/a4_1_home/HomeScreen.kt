package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.MockNumberingText
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.SummaryActivity
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.Utils
import java.nio.file.WatchEvent


@Composable
fun HomeScreen(
    name: String,
    viewModel: HomeViewModel,
    uiState: UiStateHome,
    onTabOther: () -> Unit,
) {

    val context = LocalContext.current.applicationContext
    val activity = LocalActivityContext.current

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    DefaultMonoBg(
        color = MaterialTheme.colorScheme.surface,
        content = {

            // 상태별 이미지 표시 부분
            Column(
                modifier = Modifier.padding(
                    start = 25.dp
                ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painterResource(
                        // todo. 내일 유저 학습 시작 시간이 되게되면 Available 로 상태 변경하게 하기
                        id = if (uiState.snackState == SnackState.Available)
                            R.drawable.main_back_eat_before
                        else R.drawable.main_back_eat_after
                    ),
                    contentDescription = "",
                    Modifier.size(width = 500.dp, height = 520.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                Utils.UxUtils.moveActivity(
                                    activity,
                                    SummaryActivity::class.java,
                                    exitFlag = false
                                )
                                // 요약글 조회 화면 이동
                                // 문서타입인지 카테고리 타입인지 어케암?
                                // pref 가져와서
                            }
                        )
                ) {
                    Image(
                        painterResource(
                            // todo. 내일 유저 학습 시작 시간이 되게되면 Available 로 상태 변경하게 하기
                            id = if (uiState.snackState == SnackState.Available)
                                R.drawable.img_main_food
                            else R.drawable.img_food_before
                        ),
                        contentDescription = "",
                    )
                }
            }
            // 상태별 이미지 표시 부분


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // MockNumberingText("메인")

                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

    val fakeViewModel = remember { HomeViewModel() }
    TeumTeumEatTheme {
        HomeScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateHome(),
            onTabOther = {},
        )
    }
}