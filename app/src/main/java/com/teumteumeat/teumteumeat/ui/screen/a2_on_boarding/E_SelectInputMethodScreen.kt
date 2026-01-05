package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onesignal.OneSignal
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.BoxButtonRadioGroup
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.enum_type.GoalType
import com.teumteumeat.teumteumeat.ui.theme.Typography


@Composable
fun SelectInputMethodScreen(
    name: String = "",
    viewModel: OnBoardingViewModel,
    uiState: UiStateOnBoardingMain,
    onNextFileUpload: () -> Unit,
    onPrev: () -> Unit,
    onNextCateGorySelct: () -> Unit,
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
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))
                    Text(
                        "학습할 방법을 선택 하세요!",
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

                    BoxButtonRadioGroup(
                        selectedType = uiState.goalType,
                        onSelected = { viewModel.selectLearningMethod(it) },
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BaseFillButton(
                        text = "다음으로",
                        textStyle = Typography.labelMedium.copy(
                            lineHeight = 24.sp
                        ),
                        isEnabled = uiState.goalType != GoalType.NONE,
                        onClick = {
                            viewModel.resetCategorySelection()
                            viewModel.onFileDeleted()
                            when(uiState.goalType){
                                GoalType.CATEGORY -> onNextCateGorySelct()
                                GoalType.DOCUMENT -> onNextFileUpload()
                                GoalType.NONE -> {}
                            }
                        },
                        conerRadius = 16.dp
                    )
                }
            }
        },
    )
}