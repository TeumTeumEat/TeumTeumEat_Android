package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_6_guide_expired_goal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.AddGoalActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.GoalRegisterArgs
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.Utils

class GuideExpiredGoalActivity : ComponentActivity() {

    private val goalType: DomainGoalType by lazy {
        intent.getStringExtra(GoalRegisterArgs.KEY_GOAL_TYPE)
            ?.let { DomainGoalType.valueOf(it) }
            ?: error("DomainGoalType 이 전달되지 않았습니다.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            TeumTeumEatTheme {
                GuideExpiredGoalScreen(
                    onCloseClick = { this.finish() },
                    onCreateNewGoalClick = { domainGoalType ->
                        when(domainGoalType) {
                            DomainGoalType.DOCUMENT -> {
                                this.startActivity(
                                    Intent(this, AddGoalActivity::class.java).apply {
                                        putExtra(
                                            GoalRegisterArgs.KEY_GOAL_TYPE,
                                            DomainGoalType.DOCUMENT.name // ✅ String 전달
                                        )
                                    }
                                )
                            }
                            DomainGoalType.CATEGORY -> {
                                this.startActivity(
                                    Intent(this, AddGoalActivity::class.java).apply {
                                        putExtra(
                                            GoalRegisterArgs.KEY_GOAL_TYPE,
                                            DomainGoalType.CATEGORY.name // ✅ String 전달
                                        )
                                    }
                                )
                            }
                        }

                        finish()
                    },
                )
            }
        }
    }
}