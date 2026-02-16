package com.teumteumeat.teumteumeat.localdata.preference

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomePreference @Inject constructor(
    @ApplicationContext context: Context
) {

    companion object {
        private const val PREF_NAME = "home_pref"
        private const val KEY_SNACK_CONSUMED_DATE = "snack_consumed_date"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * 오늘 이미 요약글을 사용했는지
     */
    fun isSnackConsumedToday(): Boolean {
        val savedDate = prefs.getString(KEY_SNACK_CONSUMED_DATE, null)
        val today = LocalDate.now().toString()
        return savedDate == today
    }

    /**
     * 요약글 사용 처리 (Consumed)
     */
    fun markSnackConsumedToday() {
        prefs.edit()
            .putString(KEY_SNACK_CONSUMED_DATE, LocalDate.now().toString())
            .apply()
    }

    /**
     * 자정 초기화
     */
    fun clearSnackState() {
        prefs.edit()
            .remove(KEY_SNACK_CONSUMED_DATE)
            .apply()
    }
}
