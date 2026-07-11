package com.teumteumeat.teumteumeat.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teumteumeat.teumteumeat.di.GoalTrackingPreferences
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private const val GOAL_TRACKING_DATASTORE_NAME = "goal_tracking"

val Context.goalTrackingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = GOAL_TRACKING_DATASTORE_NAME
)

/**
 * GOAL-001 `course_complete` 이벤트의 `is_first_complete` 판단을 위한 목표 완주 이력 저장소.
 *
 * - `completed_goal_ids`: 완주 화면(SubjectCompleteScreen) 진입 시 goalId 누적
 *
 * 앱 재설치·데이터 초기화 시 이력이 소실되어 is_first_complete이 다시 true로 재발생하는 것은
 * QuizTrackingDataStore의 entry_type 정책과 동일한 의도된 동작.
 */
/** GOAL-002 `next_course_start` 이벤트의 `prev_*` 파라미터 복원을 위한 직전 완주 목표 스냅샷 */
data class LastCompletedGoal(
    val goalId: String,
    val categoryId: String,
    val learningType: String,
    val isFirstComplete: String,
)

@Singleton
class GoalTrackingDataStore @Inject constructor(
    @GoalTrackingPreferences private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val COMPLETED_GOAL_IDS = stringSetPreferencesKey("completed_goal_ids")
        private val LAST_COMPLETED_GOAL_ID = stringPreferencesKey("last_completed_goal_id")
        private val LAST_COMPLETED_CATEGORY_ID = stringPreferencesKey("last_completed_category_id")
        private val LAST_COMPLETED_LEARNING_TYPE = stringPreferencesKey("last_completed_learning_type")
        private val LAST_COMPLETED_IS_FIRST = stringPreferencesKey("last_completed_is_first")
    }

    /**
     * 완주 화면 진입 시 호출. goalId가 이력에 없으면 최초 완주로 판단해 true를 반환하고
     * 이력에 추가하며, 이미 있으면 재완주로 false를 반환한다.
     */
    suspend fun resolveAndMarkFirstComplete(goalId: String): Boolean {
        val prefs = dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()
        val completedIds = prefs[COMPLETED_GOAL_IDS] ?: emptySet()

        return if (!completedIds.contains(goalId)) {
            dataStore.edit { it[COMPLETED_GOAL_IDS] = completedIds + goalId }
            true
        } else {
            false
        }
    }

    /**
     * GOAL-001 `course_complete` 로깅 직후 호출 — GOAL-002 `next_course_start`의
     * `prev_*` 파라미터 복원을 위해 방금 완주한 목표의 스냅샷을 저장한다.
     */
    suspend fun saveLastCompletedGoal(
        goalId: String,
        categoryId: String,
        learningType: String,
        isFirstComplete: Boolean,
    ) {
        dataStore.edit { prefs ->
            prefs[LAST_COMPLETED_GOAL_ID] = goalId
            prefs[LAST_COMPLETED_CATEGORY_ID] = categoryId
            prefs[LAST_COMPLETED_LEARNING_TYPE] = learningType
            prefs[LAST_COMPLETED_IS_FIRST] = isFirstComplete.toString()
        }
    }

    /**
     * `AddGoalActivity` 진입 시 조회한다 (읽기 전용, 소비하지 않음).
     * 완주 이력이 없으면 null을 반환한다.
     */
    suspend fun getLastCompletedGoal(): LastCompletedGoal? {
        val prefs = dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()
        val goalId = prefs[LAST_COMPLETED_GOAL_ID] ?: return null

        return LastCompletedGoal(
            goalId = goalId,
            categoryId = prefs[LAST_COMPLETED_CATEGORY_ID] ?: "",
            learningType = prefs[LAST_COMPLETED_LEARNING_TYPE] ?: "",
            isFirstComplete = prefs[LAST_COMPLETED_IS_FIRST] ?: "false",
        )
    }

    /**
     * `next_course_start`가 실제로 발화된 직후에만 호출 — 동일 완주 건에 대한 중복 발화를 막는다.
     * 목표 타입을 선택하지 않고 이탈한 경우에는 호출하지 않아, 다음 진짜 시도에서 재사용할 수 있다.
     */
    suspend fun clearLastCompletedGoal() {
        dataStore.edit { prefs ->
            prefs.remove(LAST_COMPLETED_GOAL_ID)
            prefs.remove(LAST_COMPLETED_CATEGORY_ID)
            prefs.remove(LAST_COMPLETED_LEARNING_TYPE)
            prefs.remove(LAST_COMPLETED_IS_FIRST)
        }
    }
}
