package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalOnBoardingMainUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import kotlinx.coroutines.flow.collectLatest


@Composable
fun OnBoardingNavHost(navController: NavHostController) {
    val activity = LocalActivityContext.current as OnBoardingActivity
    val viewModel = LocalViewModelContext.current as OnBoardingViewModel
    val uiState = LocalOnBoardingMainUiState.current
    val sessionManager = viewModel.sessionManager // 세션메니저 정의

    // 🔥 전역 세션 이벤트 감지
    LaunchedEffect(Unit) {
        sessionManager.sessionEvent.collectLatest {
            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (BuildConfig.DEBUG) OnBoardingScreens.WelcomeScreen.route
            else OnBoardingScreens.WelcomeScreen.route
    ) {

        // 1. 웰컴화면
        composable(
            route = OnBoardingScreens.WelcomeScreen.route,
        ) {
            OnBoardingWelcomeScreen(
                uiState = uiState,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.SetRoutineScreen)
                    navController.navigate(OnBoardingScreens.SetRoutineScreen.route)
                },
            )
        }

        // 2. 학습 분량 및 알림 시간 설정 화면
        composable(
            route = OnBoardingScreens.SetRoutineScreen.route,
        ) {
            OnBoardingSetRoutineScreen(
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.SelectLearningMethodScreen)
                    navController.navigate(OnBoardingScreens.SelectLearningMethodScreen.route)
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.WelcomeScreen)
                    navController.navigate(OnBoardingScreens.WelcomeScreen.route) {
                        popUpTo(OnBoardingScreens.WelcomeScreen.route) { inclusive = true }
                    }
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // 2. 학습 방법 지정 화면
        composable(
            route = OnBoardingScreens.SelectLearningMethodScreen.route
        ) {
            SelectLearningMethodScreen(
                name = OnBoardingScreens.SelectLearningMethodScreen.route,
                onNextFileUpload = {
                    viewModel.navigateTo(OnBoardingScreens.UploadFileScreen)
                    navController.navigate(OnBoardingScreens.UploadFileScreen.route)
                },
                onNextCateGorySelct = {
                    viewModel.navigateTo(OnBoardingScreens.SelectCategoryScreen)
                    navController.navigate(OnBoardingScreens.SelectCategoryScreen.route)
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.SetRoutineScreen)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // 2-1. 카테고리 선택 화면
        composable(
            route = OnBoardingScreens.SelectCategoryScreen.route
        ) { backStackEntry ->
            CategorySelectScreen(
                name = OnBoardingScreens.SelectCategoryScreen.route,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.OptimizeDataScreen)
                    navController.navigate(OnBoardingScreens.OptimizeDataScreen.route)
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.SelectLearningMethodScreen)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
                navBackStackEntry = backStackEntry,
            )
        }

        // 2-2. 파일 업로드 입력 화면
        composable(
            route = OnBoardingScreens.UploadFileScreen.route
        ) {
            FileUploadScreen(
                name = OnBoardingScreens.UploadFileScreen.route,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.OptimizeDataScreen)
                    navController.navigate(OnBoardingScreens.OptimizeDataScreen.route)
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.SelectLearningMethodScreen)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // ✅ 3. 퀴즈 및 학습의 난이도/스타일 설정
        composable(
            route = OnBoardingScreens.OptimizeDataScreen.route
        ) {
            OptimizeDataScreen(
                name = OnBoardingScreens.OptimizeDataScreen.route,
                viewModel = viewModel,
                uiState = uiState,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.SetStudyPeriodScreen)
                    navController.navigate(OnBoardingScreens.SetStudyPeriodScreen.route)
                },
                onPrev = {
                    val prev = OnBoardingScreens.fromRoute(
                        navController.previousBackStackEntry?.destination?.route
                    )
                    if (prev != null) viewModel.navigateTo(prev)
                    navController.popBackStack()
                },
                onCloseSheet = viewModel::closeBottomSheet,
                onConfirmPrompt = viewModel::onConfirmPromptOption,
                setSheetTitle = "요청 프롬프트 선택",
                onOpenPromptSheet = { viewModel.openBottomSheet(BottomSheetType.PROMPT) },
            )
        }

        // ✅ 4. 학습 기간 설정 화면
        composable(
            route = OnBoardingScreens.SetStudyPeriodScreen.route
        ) {
            SetStudyAmountScreen(
                name = OnBoardingScreens.SetStudyPeriodScreen.route,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.ReviewScreen)
                    navController.navigate(OnBoardingScreens.ReviewScreen.route)
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.OptimizeDataScreen)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // ✅ 5. 설정 정보 확인 화면
        composable(
            route = OnBoardingScreens.ReviewScreen.route
        ) {
            ReviewScreen(
                onNext = {
                    navController.navigate(OnBoardingScreens.CompleteScreen.route) {
                        // 🔑 온보딩 스택 정리 (뒤로가기 방지)
                        popUpTo(OnBoardingScreens.WelcomeScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.SetStudyPeriodScreen)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

    }
}

