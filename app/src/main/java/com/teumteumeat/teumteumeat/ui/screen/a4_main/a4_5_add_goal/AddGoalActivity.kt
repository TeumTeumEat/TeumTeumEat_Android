package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import com.teumteumeat.teumteumeat.domain.model.common.GoalType
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import dagger.hilt.android.AndroidEntryPoint

object GoalRegisterArgs {
    const val KEY_GOAL_TYPE = "key_goal_type"
}

@AndroidEntryPoint
class AddGoalActivity : ComponentActivity() {
    private val viewModel: AddGoalViewModel by viewModels()

    // 1. Intent로부터 String을 받아 DomainGoalType Enum으로 변환
    private val initialGoalType: DomainGoalType? by lazy {
        val typeString = intent.getStringExtra(GoalRegisterArgs.KEY_GOAL_TYPE)
        runCatching {
            DomainGoalType.valueOf(typeString ?: "")
        }.getOrDefault(null) // 없거나 잘못된 값이면 NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1️⃣ 외부 진입 데이터가 있다면 뷰모델 초기화
        initialGoalType?.let { viewModel.initGoalType(it) }

        setContent {
            TeumTeumEatTheme {
                // 2️⃣ hiltViewModel() 사용 시, 현재 Activity 스코프의 뷰모델을 공유하도록 설정 가능
                val viewModel: AddGoalViewModel = hiltViewModel()

                // 2. initialGoalType에 따라 시작 route 결정
                val startRoute = when (initialGoalType) {
                    DomainGoalType.CATEGORY -> GoalTypeUiState.CATEGORY
                    DomainGoalType.DOCUMENT -> GoalTypeUiState.DOCUMENT
                    else -> GoalTypeUiState.NONE
                }

                AddCategoryGoalCompositionProvider(
                    viewModel = viewModel,
                    startRoute = startRoute, // 시작 경로 전달
                    activity = this@AddGoalActivity
                )
            }
        }
    }
}
