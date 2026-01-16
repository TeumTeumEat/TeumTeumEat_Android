package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import dagger.hilt.android.AndroidEntryPoint

object GoalRegisterArgs {
    const val KEY_GOAL_TYPE = "key_goal_type"
}

@AndroidEntryPoint
class AddGoalActivity : ComponentActivity() {
    // ✅ Activity 전역 ViewModel
    private val viewModel: AddGoalViewModel by viewModels()

    private val goalType: DomainGoalType by lazy {
        intent.getStringExtra(GoalRegisterArgs.KEY_GOAL_TYPE)
            ?.let { DomainGoalType.valueOf(it) }
            ?: error("DomainGoalType 이 전달되지 않았습니다.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ 여기서 진입 타입 초기화
        viewModel.initGoalType(goalType)

        setContent {
            TeumTeumEatTheme {
                val viewModel: AddGoalViewModel = hiltViewModel()
                AddCategoryGoalCompositionProvider(
                    viewModel = viewModel,
                    context = this.applicationContext,
                    activity = this@AddGoalActivity
                )
            }
        }
    }
}
