package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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
        startDestination = OnBoardingScreens.FirstScreen.route
    ) {

        composable(
            route = OnBoardingScreens.FirstScreen.route,
        ) {
            OnBoardingFirstScreen(
                uiState = uiState,
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(OnBoardingScreens.SecondInputNameScreen.route)
                },
            )
        }

        composable(
            route = OnBoardingScreens.SecondInputNameScreen.route,
        ) {
            OnBoardingSetCharNameScreen(
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(OnBoardingScreens.ThirdSetAppTimeScreen.route)
                },

                onPrev = {
                    navController.navigate(OnBoardingScreens.FirstScreen.route) {
                        viewModel.prevPage()
                        popUpTo(OnBoardingScreens.FirstScreen.route) { inclusive = true }
                    }
                },
                name = "InputName",
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        composable(
            route = OnBoardingScreens.ThirdSetAppTimeScreen.route,
        ) {
            OnBoardingSetApptimeScreen(
                onNext = {
                    viewModel.nextPage()
                    // 4번째 화면 이동 로직 구현
                    navController.navigate(OnBoardingScreens.FourthSetUsingAppTimeScreen.route)
                },

                onPrev = {
                    navController.navigate(OnBoardingScreens.SecondInputNameScreen.route) {
                        viewModel.prevPage()
                        popUpTo(OnBoardingScreens.SecondInputNameScreen.route) { inclusive = true }
                    }
                },
                name = "set_app_time",
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // 4️⃣ 앱 사용 시간 관련 설정 화면
        composable(
            route = OnBoardingScreens.FourthSetUsingAppTimeScreen.route
        ) {
            OnBoardingSetUsingApptimeScreen(
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(OnBoardingScreens.FifthSelectInputMethodScreen.route)
                },
                onPrev = {
                    viewModel.prevPage()
                    navController.popBackStack()
                },
                name = "set_using_app_time",
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // 5️⃣ 정보 입력 방법 지정 화면
        composable(
            route = OnBoardingScreens.FifthSelectInputMethodScreen.route
        ) {
            SelectInputMethodScreen(
                name = OnBoardingScreens.FifthSelectInputMethodScreen.route,
                onNextFileUpload = {
                    navController.navigate(OnBoardingScreens.SixthFileUploadScreen.route)
                },

                onNextCateGorySelct = {
                    navController.navigate(OnBoardingScreens.SixthCategorySelectScreen.route)
                },
                onPrev = {
                    viewModel.prevPage()
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
                    viewModel.nextPage()
                    navController.navigate(OnBoardingScreens.SeventhOptimizerDataScreen.route)
                },
                onPrev = {
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
                    viewModel.nextPage()
                    navController.navigate(OnBoardingScreens.SeventhOptimizerDataScreen.route)
                },
                onPrev = {
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
                    navController.navigate(
                        OnBoardingScreens.EighthSetStudyRangeScreen.route
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

        // ✅ 8. 학습 범위 설정 화면
        composable(
            route = OnBoardingScreens.EighthSetStudyRangeScreen.route
        ) {
            SetStudyRangeScreen(
                name = OnBoardingScreens.EighthSetStudyRangeScreen.route,
                onNext = {
                    navController.navigate(
                        OnBoardingScreens.CheckSetMyInfoScreen.route
                    )
                },
                onPrev = {
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
                        popUpTo(OnBoardingScreens.FirstScreen.route) {
                            inclusive = true
                        }
                    }
                },
                onPrev = {
                    navController.popBackStack()
                },
                viewModel = viewModel,
                uiState = uiState,
            )
        }

    }
}

sealed class OnBoardingScreens(val route: String) {

    data object FirstScreen :
        OnBoardingScreens("welcome")

    data object SecondInputNameScreen :
        OnBoardingScreens("input_name")

    data object ThirdSetAppTimeScreen :
        OnBoardingScreens("set_app_time")

    data object FourthSetUsingAppTimeScreen :
        OnBoardingScreens("set_using_app_time")

    data object FifthSelectInputMethodScreen :
        OnBoardingScreens("select_input_method")

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
}

object OnBoardingFlow {

    /** ✅ 온보딩 순서의 단일 소스 */
    val screens: List<OnBoardingScreens> = listOf(
        OnBoardingScreens.FirstScreen,
        OnBoardingScreens.SecondInputNameScreen,
        OnBoardingScreens.ThirdSetAppTimeScreen,
        OnBoardingScreens.FourthSetUsingAppTimeScreen,
        OnBoardingScreens.FifthSelectInputMethodScreen,
        OnBoardingScreens.SixthCategorySelectScreen,
        OnBoardingScreens.EighthSetStudyRangeScreen,
        OnBoardingScreens.CheckSetMyInfoScreen,
    )

    /** 전체 페이지 수 */
    val totalCount: Int
        get() = 5

    /** 현재 페이지 (1부터 시작)
     * FirstScreen 은 카운트에 포함X
     * */
    fun currentPage(screen: OnBoardingScreens): Int {
        return screens.indexOf(screen)
    }

    /** 이전 화면 */
    fun prev(screen: OnBoardingScreens): OnBoardingScreens? {
        val index = screens.indexOf(screen)
        return screens.getOrNull(index - 1)
    }

    /** 다음 화면 */
    fun next(screen: OnBoardingScreens): OnBoardingScreens? {
        val index = screens.indexOf(screen)
        return screens.getOrNull(index + 1)
    }
}

