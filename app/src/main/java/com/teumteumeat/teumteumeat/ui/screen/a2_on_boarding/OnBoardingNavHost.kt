package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalOnBoardingMainUiState
import com.teumteumeat.teumteumeat.utils.Utils
import kotlinx.coroutines.flow.collectLatest
import kotlin.jvm.java


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
        startDestination = OnBoardingScreens.WelcomeScreen.route
    ) {

        // 1. 웰컴화면
        composable(
            route = OnBoardingScreens.WelcomeScreen.route,
        ) {
            OnBoardingWelcomeScreen(
                uiState = uiState,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.OnboardingSetRoutineScreen)
                    navController.navigate(OnBoardingScreens.OnboardingSetRoutineScreen.route)
                },
            )
        }

        // 2. 학습 분량 및 알림 시간 설정 화면
        composable(
            route = OnBoardingScreens.OnboardingSetRoutineScreen.route,
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

        // 3️⃣ 학습 방법 지정 화면
        composable(
            route = OnBoardingScreens.SelectLearningMethodScreen.route
        ) {
            SelectLearningMethodScreen(
                name = OnBoardingScreens.SelectLearningMethodScreen.route,
                onNextFileUpload = {
                    viewModel.navigateTo(OnBoardingScreens.SixthFileUploadScreen)
                    navController.navigate(OnBoardingScreens.SixthFileUploadScreen.route)
                },
                onNextCateGorySelct = {
                    viewModel.navigateTo(OnBoardingScreens.SixthCategorySelectScreen)
                    navController.navigate(OnBoardingScreens.SixthCategorySelectScreen.route)
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.OnboardingSetRoutineScreen)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // ✅ 6-1 카테고리 선택 화면
        composable(
            route = OnBoardingScreens.SixthCategorySelectScreen.route
        ) { backStackEntry ->
            CategorySelectScreen(
                name = OnBoardingScreens.SixthCategorySelectScreen.route,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.SeventhOptimizerDataScreen)
                    navController.navigate(OnBoardingScreens.SeventhOptimizerDataScreen.route)
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

        // ✅ 6-2 파일 업로드 입력 화면
        composable(
            route = OnBoardingScreens.SixthFileUploadScreen.route
        ) {
            FileUploadScreen(
                name = OnBoardingScreens.SixthFileUploadScreen.route,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.SeventhOptimizerDataScreen)
                    navController.navigate(OnBoardingScreens.SeventhOptimizerDataScreen.route)
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.SelectLearningMethodScreen)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // ✅ 7. 옵티마이저 데이터 입력 화면
        composable(
            route = OnBoardingScreens.SeventhOptimizerDataScreen.route
        ) {
            OptimizerDataScreen(
                name = OnBoardingScreens.SeventhOptimizerDataScreen.route,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.EighthSetStudyRangeScreen)
                    navController.navigate(OnBoardingScreens.EighthSetStudyRangeScreen.route)
                },
                onPrev = {
                    val prev = OnBoardingScreens.fromRoute(
                        navController.previousBackStackEntry?.destination?.route
                    )
                    if (prev != null) viewModel.navigateTo(prev)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // ✅ 8. 학습 범위 설정 화면
        composable(
            route = OnBoardingScreens.EighthSetStudyRangeScreen.route
        ) {
            SetStudyRangeScreen(
                name = OnBoardingScreens.EighthSetStudyRangeScreen.route,
                onNext = {
                    viewModel.navigateTo(OnBoardingScreens.CheckSetMyInfoScreen)
                    navController.navigate(OnBoardingScreens.CheckSetMyInfoScreen.route)
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.SeventhOptimizerDataScreen)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // ✅ 9. 내 정보 확인 화면
        composable(
            route = OnBoardingScreens.CheckSetMyInfoScreen.route
        ) {
            CheckSetMyInfoScreen(
                onNext = {
                    navController.navigate(OnBoardingScreens.CompleteScreen.route) {
                        // 🔑 온보딩 스택 정리 (뒤로가기 방지)
                        popUpTo(OnBoardingScreens.WelcomeScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onPrev = {
                    viewModel.navigateTo(OnBoardingScreens.EighthSetStudyRangeScreen)
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

    }
}

sealed class OnBoardingScreens(val route: String) {

    data object WelcomeScreen :
        OnBoardingScreens("welcome")

    data object OnboardingSetRoutineScreen :
        OnBoardingScreens("set_app_time")

    data object SelectLearningMethodScreen :
        OnBoardingScreens("select_learning_method")

    data object SixthCategorySelectScreen :
        OnBoardingScreens("select_category")

    data object SixthFileUploadScreen :
        OnBoardingScreens("file_upload")

    data object SeventhOptimizerDataScreen :
        OnBoardingScreens("optimizer_data")

    data object EighthSetStudyRangeScreen :
        OnBoardingScreens("set_study_range")

    data object CheckSetMyInfoScreen :
        OnBoardingScreens("check_set_my_info")

    data object CompleteScreen :
        OnBoardingScreens("complete")

    companion object {
        private val all by lazy {
            listOf(
                WelcomeScreen, OnboardingSetRoutineScreen,
                SelectLearningMethodScreen,
                SixthCategorySelectScreen, SixthFileUploadScreen,
                SeventhOptimizerDataScreen, EighthSetStudyRangeScreen,
                CheckSetMyInfoScreen, CompleteScreen,
            )
        }
        fun fromRoute(route: String?): OnBoardingScreens? {
            if (route == null) return null
            return all.firstOrNull { it.route == route }
        }
    }
}

object OnBoardingFlow {

    private val screens: List<OnBoardingScreens> = listOf(
        OnBoardingScreens.WelcomeScreen,
        OnBoardingScreens.OnboardingSetRoutineScreen,
        OnBoardingScreens.SelectLearningMethodScreen,
        OnBoardingScreens.SixthCategorySelectScreen,
        OnBoardingScreens.SeventhOptimizerDataScreen,
        OnBoardingScreens.EighthSetStudyRangeScreen,
        OnBoardingScreens.CheckSetMyInfoScreen,
    )

    private val pageMap: Map<OnBoardingScreens, Int> = mapOf(
        OnBoardingScreens.WelcomeScreen to 0,
        OnBoardingScreens.OnboardingSetRoutineScreen to 1,
        OnBoardingScreens.SelectLearningMethodScreen to 2,
        OnBoardingScreens.SixthCategorySelectScreen to 2,
        OnBoardingScreens.SixthFileUploadScreen to 2,
        OnBoardingScreens.SeventhOptimizerDataScreen to 3,
        OnBoardingScreens.EighthSetStudyRangeScreen to 4,
        OnBoardingScreens.CheckSetMyInfoScreen to 5,
    )

    const val MAX_PAGE_INDEX: Int = 5

    fun currentPage(screen: OnBoardingScreens): Int = pageMap[screen] ?: 0

    fun prev(screen: OnBoardingScreens): OnBoardingScreens? {
        val index = screens.indexOf(screen)
        return screens.getOrNull(index - 1)
    }

    fun next(screen: OnBoardingScreens): OnBoardingScreens? {
        val index = screens.indexOf(screen)
        return screens.getOrNull(index + 1)
    }
}

