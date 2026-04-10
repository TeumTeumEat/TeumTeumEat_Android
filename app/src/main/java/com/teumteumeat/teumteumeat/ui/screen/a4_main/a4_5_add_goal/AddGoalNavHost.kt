package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAddGoalUiState


@Composable
fun AddGoalNavHost(
    navController: NavHostController,
    startDestination: GoalTypeUiState,
) {
    val activity = LocalActivityContext.current as AddGoalActivity
    val viewModel = LocalViewModelContext.current as AddGoalViewModel
    val uiState = LocalAddGoalUiState.current


    val startRoute = when (startDestination) {
        GoalTypeUiState.CATEGORY ->  AddGoalScreens.SixthCategorySelectScreen.route
        GoalTypeUiState.DOCUMENT -> AddGoalScreens.SixthFileUploadScreen.route
        GoalTypeUiState.NONE -> AddGoalScreens.SelectInputMethodScreen.route
    }


    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect {
            Log.d("NAV_DEBUG", "현재 route=${it.destination.route}")
        }
    }


    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {

        // ✅ 1-0. 목표 타입 선택 화면
        composable(
            route = AddGoalScreens.SelectInputMethodScreen.route
        ) {

            AddGoalBackHandler(
                currentPage = uiState.currentPage,
                navController = navController,
                // 💡 라디오 버튼은 상태만 바꾸고, 이동은 이 콜백을 통해 버튼 클릭 시 수행
                onFinish = { activity.finish() },
                onPrevPage = { viewModel.prevPage() }
            )

            SelectInputMethodScreen(
                viewModel = viewModel,
                uiState = uiState,
                onPrev = {
                    activity.finish()
                },

                onNextFileUpload = {
                    viewModel.nextPage()
                    navController.navigate(AddGoalScreens.SixthFileUploadScreen.route)
                },
                // 요청하신 구현 부분: 선택된 타입에 따라 분기 처리
                onNextCateGorySelect = {
                    viewModel.nextPage() // 페이지 번호 증가
                    navController.navigate(AddGoalScreens.SixthCategorySelectScreen.route)
                }
            )
        }

        // ✅ 1-1 카테고리 선택 화면
        composable(
            route = AddGoalScreens.SixthCategorySelectScreen.route
        ) { backStackEntry ->

            AddGoalBackHandler(
                currentPage = uiState.currentPage,
                navController = navController,
                onFinish = { activity.finish() },
                onPrevPage = { viewModel.prevPage() },
            )

            AddGoalCategorySelectScreen(
                name = AddGoalScreens.SixthCategorySelectScreen.route,
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
                onPrevPage = { viewModel.prevPage() },
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
                onPrevPage = { viewModel.prevPage() },
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
                onPrevPage = { viewModel.prevPage() },
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
                onPrevPage = { viewModel.prevPage() },
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

    data object SelectInputMethodScreen :
        AddGoalScreens("select_input_method")

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
        AddGoalScreens.SelectInputMethodScreen,
        AddGoalScreens.SixthCategorySelectScreen,
        AddGoalScreens.SixthFileUploadScreen,
        AddGoalScreens.SeventhOptimizerDataScreen,
        AddGoalScreens.EighthSetStudyRangeScreen,
        AddGoalScreens.CheckSetMyInfoScreen,
    )

    /** 전체 페이지 수 */
    fun totalCount(goalType: GoalTypeUiState): Int {
        return if (goalType == GoalTypeUiState.NONE) 5 else 4
    }

    /** 현재 페이지 (1부터 시작)
     * FirstScreen 은 카운트에 포함X
     * */
    fun currentPage(screen: AddGoalScreens, goalType: GoalTypeUiState): Int {
        return when (screen) {
            AddGoalScreens.SelectInputMethodScreen -> 1
            AddGoalScreens.SixthCategorySelectScreen,
            AddGoalScreens.SixthFileUploadScreen -> if (goalType == GoalTypeUiState.NONE) 2 else 1
            AddGoalScreens.SeventhOptimizerDataScreen -> if (goalType == GoalTypeUiState.NONE) 3 else 2
            AddGoalScreens.EighthSetStudyRangeScreen -> if (goalType == GoalTypeUiState.NONE) 4 else 3
            AddGoalScreens.CheckSetMyInfoScreen -> if (goalType == GoalTypeUiState.NONE) 5 else 4
            else -> 0
        }
    }

    /** 이전 화면 */
    fun prev(screen: AddGoalScreens, goalType: GoalTypeUiState): AddGoalScreens? {
        return when (screen) {
            AddGoalScreens.SixthCategorySelectScreen,
            AddGoalScreens.SixthFileUploadScreen -> {
                if (goalType == GoalTypeUiState.NONE) AddGoalScreens.SelectInputMethodScreen else null
            }
            AddGoalScreens.SeventhOptimizerDataScreen -> {
                if (goalType == GoalTypeUiState.CATEGORY) AddGoalScreens.SixthCategorySelectScreen
                else AddGoalScreens.SixthFileUploadScreen
            }
            AddGoalScreens.EighthSetStudyRangeScreen -> AddGoalScreens.SeventhOptimizerDataScreen
            AddGoalScreens.CheckSetMyInfoScreen -> AddGoalScreens.EighthSetStudyRangeScreen
            else -> null
        }
    }

    /** 다음 화면 */
    fun next(screen: AddGoalScreens, goalType: GoalTypeUiState): AddGoalScreens? {
        return when (screen) {
            AddGoalScreens.SelectInputMethodScreen -> {
                when (goalType) {
                    GoalTypeUiState.CATEGORY -> AddGoalScreens.SixthCategorySelectScreen
                    GoalTypeUiState.DOCUMENT -> AddGoalScreens.SixthFileUploadScreen
                    else -> null
                }
            }
            AddGoalScreens.SixthCategorySelectScreen,
            AddGoalScreens.SixthFileUploadScreen -> AddGoalScreens.SeventhOptimizerDataScreen
            AddGoalScreens.SeventhOptimizerDataScreen -> AddGoalScreens.EighthSetStudyRangeScreen
            AddGoalScreens.EighthSetStudyRangeScreen -> AddGoalScreens.CheckSetMyInfoScreen
            else -> null
        }
    }
}

