package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.theme.Typography


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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "서비스소개소개소개",
                        style = Typography.headlineMedium.copy(
                            fontSize = 18.sp,
                        )
                    )

                    Spacer(modifier = Modifier.height(100.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center

                    ) {
                        Image(
                            painter = painterResource(R.drawable.character_front),
                            contentDescription = "앞을 보는 케릭터",
                            contentScale = ContentScale.Fit,
                        )
                    }

                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BaseFillButton(
                        text = "시작하기",
                        textStyle = Typography.labelMedium.copy(
                            lineHeight = 24.sp
                        ),
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

/*
@Preview(showBackground = true)
@Composable
fun OnBoardingPreview() {

    val fakeViewModel = remember { OnBoardingViewModel() }
    TeumTeumEatTheme {
        OnBoardingFirstScreen(
            name = "Android",
            viewModel = fakeViewModel,
            uiState = UiStateOnBoardingMain(errorMessage = "한글 또는 영문만 입력할 수 있어요", isNameValid = false),
            onNext = {}
        )
    }
}*/
