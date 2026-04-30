package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

object OnBoardingFlow {

    private val screens: List<OnBoardingScreens> = listOf(
        OnBoardingScreens.WelcomeScreen,
        OnBoardingScreens.SetRoutineScreen,
        OnBoardingScreens.SelectLearningMethodScreen,
        OnBoardingScreens.SelectCategoryScreen,
        OnBoardingScreens.OptimizeDataScreen,
        OnBoardingScreens.SetStudyPeriodScreen,
        OnBoardingScreens.ReviewScreen,
    )

    private val pageMap: Map<OnBoardingScreens, Int> = mapOf(
        OnBoardingScreens.WelcomeScreen to 0,
        OnBoardingScreens.SetRoutineScreen to 1,
        OnBoardingScreens.SelectLearningMethodScreen to 2,
        OnBoardingScreens.SelectCategoryScreen to 2,
        OnBoardingScreens.UploadFileScreen to 2,
        OnBoardingScreens.OptimizeDataScreen to 3,
        OnBoardingScreens.SetStudyPeriodScreen to 4,
        OnBoardingScreens.ReviewScreen to 5,
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