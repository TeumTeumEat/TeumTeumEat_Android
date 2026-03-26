package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.CategorySelectScreen
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.CheckSetMyInfoScreen
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.FileUploadScreen
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OnBoardingViewModel
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.OptimizerDataScreen
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.SetStudyRangeScreen
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAddGoalUiState
import com.teumteumeat.teumteumeat.utils.LocalOnBoardingMainUiState


@Composable
fun AddGoalNavHost(
    navController: NavHostController,
    startDestination: String?,
) {
    val activity = LocalActivityContext.current as AddGoalActivity
    val viewModel = LocalViewModelContext.current as AddGoalViewModel
    val uiState = LocalAddGoalUiState.current


    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect {
            Log.d("NAV_DEBUG", "현재 route=${it.destination.route}")
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {

        // ✅ 1-1 카테고리 선택 화면
        composable(
            route = AddGoalScreens.SixthCategorySelectScreen.route
        ) { backStackEntry ->

            AddGoalBackHandler(
                currentPage = uiState.currentPage,
                navController = navController,
                onFinish = { activity.finish() },
                onPrevPage = { viewModel.prevPage() }
            )

            AddGoalCategorySelectScreen(
                name = AddGoalScreens.SixthCategorySelectScreen.route,
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(AddGoalScreens.SeventhOptimizerDataScreen.route)
                },
                onPrev = {
                    activity.finish()
                },
                viewModel = viewModel,
                uiState = uiState,
                navBackStackEntry = backStackEntry,
            )

        }

        // ✅ 1-2 파일 업로드 입력 화면
        composable(
            route = AddGoalScreens.SixthFileUploadScreen.route
        ) {

            AddGoalBackHandler(
                currentPage = uiState.currentPage,
                navController = navController,
                onFinish = { activity.finish() },
                onPrevPage = { viewModel.prevPage() }
            )

            AddGoalFileUploadScreen(
                name = AddGoalScreens.SixthFileUploadScreen.route,
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(AddGoalScreens.SeventhOptimizerDataScreen.route)
                },
                onPrev = {
                    viewModel.prevPage()
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // ✅ 2. 옵티마이저 데이터 입력 화면
        composable(
            route = AddGoalScreens.SeventhOptimizerDataScreen.route
        ) {

            AddGoalBackHandler(
                currentPage = uiState.currentPage,
                navController = navController,
                onFinish = { activity.finish() },
                onPrevPage = { viewModel.prevPage() }
            )

            AddGoalOptimizerDataScreen(
                name = AddGoalScreens.SeventhOptimizerDataScreen.route,
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(
                        AddGoalScreens.EighthSetStudyRangeScreen.route
                    )
                },
                onPrev = {
                    viewModel.prevPage()
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // ✅ 3. 학습 범위 설정 화면
        composable(
            route = AddGoalScreens.EighthSetStudyRangeScreen.route
        ) {

            AddGoalBackHandler(
                currentPage = uiState.currentPage,
                navController = navController,
                onFinish = { activity.finish() },
                onPrevPage = { viewModel.prevPage() }
            )

            AddGoalSetStudyRangeScreen(
                name = AddGoalScreens.EighthSetStudyRangeScreen.route,
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(
                        AddGoalScreens.CheckSetMyInfoScreen.route
                    )
                },
                onPrev = {
                    viewModel.prevPage()
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // ✅ 4. 내 정보 확인 화면
        composable(
            route = AddGoalScreens.CheckSetMyInfoScreen.route
        ) {

            AddGoalBackHandler(
                currentPage = uiState.currentPage,
                navController = navController,
                onFinish = { activity.finish() },
                onPrevPage = { viewModel.prevPage() }
            )

            AddGoalCheckSetMyInfoScreen(
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(AddGoalScreens.CompleteScreen.route) {
                        // 🔑 온보딩 스택 정리 (뒤로가기 방지)
//                        popUpTo(OnBoardingScreens.FirstScreen.route) {
//                            inclusive = true
//                        }
                    }
                },
                onPrev = {
                    viewModel.prevPage()
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }


    }
}

sealed class AddGoalScreens(val route: String) {

    data object SixthCategorySelectScreen :
        AddGoalScreens("select_category")

    data object SixthFileUploadScreen :
        AddGoalScreens("file_upload")

    data object SeventhOptimizerDataScreen :
        AddGoalScreens("optimizer_data")

    data object EighthSetStudyRangeScreen :
        AddGoalScreens("set_study_range")

    data object CheckSetMyInfoScreen :
        AddGoalScreens("check_set_my_info")

    data object CompleteScreen :
        AddGoalScreens("complete")
}

object AddGoalFlow {

    /** 목표 추가 순서 */
    val screens: List<AddGoalScreens> = listOf(
        AddGoalScreens.SixthCategorySelectScreen,
        AddGoalScreens.SixthFileUploadScreen,
        AddGoalScreens.SeventhOptimizerDataScreen,
        AddGoalScreens.EighthSetStudyRangeScreen,
        AddGoalScreens.CheckSetMyInfoScreen,
    )

    /** 전체 페이지 수 */
    val totalCount: Int
        get() = 4

    /** 현재 페이지 (1부터 시작)
     * FirstScreen 은 카운트에 포함X
     * */
    fun currentPage(screen: AddGoalScreens): Int {
        return screens.indexOf(screen) + 1
    }

    /** 이전 화면 */
    fun prev(screen: AddGoalScreens): AddGoalScreens? {
        val index = screens.indexOf(screen)
        return screens.getOrNull(index - 1)
    }

    /** 다음 화면 */
    fun next(screen: AddGoalScreens): AddGoalScreens? {
        val index = screens.indexOf(screen)
        return screens.getOrNull(index + 1)
    }
}

