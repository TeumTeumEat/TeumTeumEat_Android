package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.component.CustomProgressBar
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.SizeAnimationInvisible
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingLoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.PopupOverlay
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAddGoalUiState
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import kotlinx.coroutines.delay


@Composable
fun AddCategoryGoalCompositionProvider(
    viewModel: AddGoalViewModel,
    context: Context,
    activity: AddGoalActivity,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainState by viewModel.mainState.collectAsStateWithLifecycle()
    val navHostController = rememberNavController()

    val visibleStates = remember(mainState) {
        mutableStateListOf(false, false, false)
    }

    CompositionLocalProvider(
        LocalAppContext provides context,
        LocalActivityContext provides activity,
        LocalAddGoalUiState provides uiState,
        LocalViewModelContext provides viewModel,
    ) {
        // ✅ 항상 최상단에 위치
        PopupOverlay(
            popoUpErrorTitle = uiState.popoUpErrorTitle,
            popUpErrorMessage = uiState.popUpErrorMessage,
            onConfirm = { viewModel.clearFileError() },
            onDismiss = { viewModel.clearFileError() },
            isPrimaryBtnFillSecondary = true,
        )

        when (mainState) {

            UiStateAddGoalScreenState.Loading -> {

                LaunchedEffect(Unit) {
                    visibleStates.forEachIndexed { index, _ ->
                        delay(300)
                        visibleStates[index] = true
                    }
                }
                OnBoardingLoadingScreen(
                    title = "틈틈잇을 생성하는 중\n" +
                            "잠시만 기다려주세요",
                    visibleStates = visibleStates
                )
            }

            UiStateAddGoalScreenState.Success -> {
                AddGoalSuccessScreen(
                    onStartClick = {
                        Utils.UxUtils.moveActivity(
                            context,
                            MainActivity::class.java,
                            exitFlag = true
                        )
                    }
                )
            }

            is UiStateAddGoalScreenState.Error -> {
                val error = mainState as UiStateAddGoalScreenState.Error

                FullScreenErrorModal(
                    errorState = viewModel.getErrorState(
                        message = error.message,
                        onRetry = {
                            // ✅ 통합 API 재호출
                            viewModel.submitOnBoarding()
                        }
                    ),
                    onBack = {
                        viewModel.resetMainState()
                    }
                )
            }

            UiStateAddGoalScreenState.Idle -> {
                DefaultMonoBg(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {

                        Row(
                            modifier = Modifier.padding(
                                horizontal = 24.dp,
                            ),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            SizeAnimationInvisible(
                                isVisible = true
                            ) {
                                IconButton(
                                    onClick = {
                                        if(uiState.currentPage < 1){
                                            Log.d("uiState.currentPage", "${uiState.currentPage}")
                                            activity.finish()
                                        }else{
                                            viewModel.prevPage()
                                            navHostController.popBackStack()
                                        }
                                    },

                                    ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                        contentDescription = "previous page",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(0.dp),
                                    )
                                }
                            }


                            CustomProgressBar(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                currentStep = uiState.currentPage,
                                totalSteps = uiState.totalPage,
                            )


                            Text(
                                "${uiState.currentPage}/${uiState.totalPage}",
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        AddGoalNavHost(
                            navController = navHostController,
                            startDestination = when(uiState.goalTypeUiState){
                                GoalTypeUiState.DOCUMENT -> AddGoalScreens.SixthFileUploadScreen.route
                                GoalTypeUiState.CATEGORY -> AddGoalScreens.SixthCategorySelectScreen.route
                                // todo. 분기를 정확히 전달 못받았을 때 에러스크린 노출
                                GoalTypeUiState.NONE -> null
                            }
                        )
                    }
                }
            }

        }

    }

}