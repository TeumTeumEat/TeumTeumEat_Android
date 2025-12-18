package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.MockNumberingText
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme


@Composable
fun OnBoardingFirstScreen(
    name: String,
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnBoardingMain,
    onNext : () -> Unit,
) {

    val currentPage = uiState.currentPage
    val totalPages = uiState.totalPage

    DefaultMonoBg(
        color = MaterialTheme.colorScheme.surface,
        content = {
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
                    MockNumberingText("1")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center

                    ) {
                        Button(
                            onClick = {
                                viewModel.nextPage()
                                Log.d("1증가", "증가함, ${currentPage}/${totalPages}")
                            },
                        ) { Text("+") }

                        Button(
                            onClick = {
                                viewModel.prevPage()
                                Log.d("1감소", "감소함, ${currentPage}/${totalPages}")
                            },
                        ) { Text("-") }
                    }
                }
                BaseFillButton(
                    text = "시작하기",
                    onClick = {
                        onNext()
                    }
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun OnBoardingPreview() {

    val fakeViewModel = remember { OnBoardingViewModel() }
    TeumTeumEatTheme {
        OnBoardingFirstScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateOnBoardingMain(),
            onNext = {}
        )
    }
}