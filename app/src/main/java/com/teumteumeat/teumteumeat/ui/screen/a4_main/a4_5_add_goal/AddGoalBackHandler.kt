package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun AddGoalBackHandler(
    currentPage: Int,
    navController: NavHostController,
    onFinish: () -> Unit,
    onPrevPage: () -> Unit,
) {
    BackHandler(enabled = true) {

        if (currentPage <= 1) {
            onFinish()
        } else {
            onPrevPage()
            navController.popBackStack()
        }
    }
}