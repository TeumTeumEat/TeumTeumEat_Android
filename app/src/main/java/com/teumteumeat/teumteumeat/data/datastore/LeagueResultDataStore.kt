package com.teumteumeat.teumteumeat.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teumteumeat.teumteumeat.di.LeagueResultPreferences
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

private const val LEAGUE_RESULT_DATASTORE_NAME = "league_result"

val Context.leagueResultDataStore: DataStore<Preferences> by preferencesDataStore(
    name = LEAGUE_RESULT_DATASTORE_NAME
)

/**
 * 리그 결과 안내 모달을 '주당 1회'만 보여주기 위한 로컬 기록.
 * 서버(GET /league/results/latest)는 항상 직전 주 결과를 내려줄 뿐 확인 여부를 모르므로, 계정별로 마지막에 확인한 주의 시작일을 기기에 저장한다.
 * 계정마다 키가 다르므로 같은 기기에서 계정을 바꿔도 새 계정은 모달을 볼 수 있고, 원래 계정으로 돌아와도 이미 본 주는 다시 뜨지 않는다.
 *
 * 키: `last_seen_week_start.<userId>`. userId 를 알 수 없으면 [UNKNOWN_USER_ID] 로 모아 저장한다(계정 구분 불가 시 기존처럼 기기 단위 동작).
 * 앱 재설치·데이터 초기화 시 기록이 사라져 같은 주 결과가 한 번 더 노출될 수 있다(의도된 동작).
 */
@Singleton
class LeagueResultDataStore @Inject constructor(
    @LeagueResultPreferences private val dataStore: DataStore<Preferences>
) {

    companion object {
        private const val KEY_PREFIX = "last_seen_week_start."
        internal const val UNKNOWN_USER_ID = "unknown"

        private fun keyOf(userId: String?) = stringPreferencesKey(KEY_PREFIX + (userId ?: UNKNOWN_USER_ID))
    }

    /** [userId] 계정이 마지막으로 확인한 결과의 주 시작일. 기록이 없거나 읽을 수 없으면 null. */
    suspend fun getLastSeenWeekStart(userId: String?): LocalDate? {
        val prefs = dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()
        return prefs[keyOf(userId)].toLocalDateOrNull()
    }

    /**
     * [userId] 계정이 [weekStart] 주차 결과를 확인했음을 기록한다. 이미 더 최근 주를 확인한 기록이 있으면 덮어쓰지 않는다.
     * 디스크 쓰기에 실패하면 예외를 던지지 않고 false 를 돌려준다(호출부가 화면/탈퇴 흐름을 끊지 않도록).
     */
    suspend fun markSeen(userId: String?, weekStart: LocalDate): Boolean {
        val key = keyOf(userId)
        return writeSafely {
            dataStore.edit { prefs ->
                val current = prefs[key].toLocalDateOrNull()
                if (current == null || weekStart.isAfter(current)) prefs[key] = weekStart.toString()
            }
        }
    }

    /**
     * [userId] 계정의 기록을 지운다. 회원 탈퇴 시 해당 계정 id 가 기기에 남지 않도록 호출한다.
     * 디스크 쓰기에 실패하면 예외를 던지지 않고 false 를 돌려준다.
     */
    suspend fun clear(userId: String?): Boolean {
        val key = keyOf(userId)
        return writeSafely { dataStore.edit { prefs -> prefs.remove(key) } }
    }

    /** DataStore 쓰기 중 [IOException] 만 흡수한다. 코루틴 취소 등 다른 예외는 그대로 전파한다. */
    private suspend fun writeSafely(write: suspend () -> Unit): Boolean =
        try {
            write()
            true
        } catch (e: IOException) {
            false
        }
}

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let {
        try {
            LocalDate.parse(it)
        } catch (e: DateTimeParseException) {
            null
        }
    }
