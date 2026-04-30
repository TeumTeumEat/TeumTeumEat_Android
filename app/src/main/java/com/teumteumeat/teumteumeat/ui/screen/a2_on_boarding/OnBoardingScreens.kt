package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

sealed class OnBoardingScreens(val route: String) {

    data object WelcomeScreen :
        OnBoardingScreens("welcome")

    data object SetRoutineScreen :
        OnBoardingScreens("set_app_time")

    data object SelectLearningMethodScreen :
        OnBoardingScreens("select_learning_method")

    data object SelectCategoryScreen :
        OnBoardingScreens("select_category")

    data object UploadFileScreen :
        OnBoardingScreens("file_upload")

    data object OptimizeDataScreen :
        OnBoardingScreens("optimizer_data")

    data object SetStudyPeriodScreen :
        OnBoardingScreens("set_study_period")

    data object ReviewScreen :
        OnBoardingScreens("review")

    data object CompleteScreen :
        OnBoardingScreens("complete")

    companion object {
        private val all by lazy {
            listOf(
                WelcomeScreen, SetRoutineScreen,
                SelectLearningMethodScreen,
                SelectCategoryScreen, UploadFileScreen,
                OptimizeDataScreen, SetStudyPeriodScreen,
                ReviewScreen, CompleteScreen,
            )
        }

        fun fromRoute(route: String?): OnBoardingScreens? {
            if (route == null) return null
            return all.firstOrNull { it.route == route }
        }
    }
}