package com.teumteumeat.teumteumeat.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
@Singleton
class GoalTrackingDataStore @Inject constructor(
    @GoalTrackingPreferences private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val COMPLETED_GOAL_IDS = stringSetPreferencesKey("completed_goal_ids")
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
}
