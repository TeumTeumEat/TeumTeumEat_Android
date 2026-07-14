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
        private const val KEY_SELECTED_FOOD_RES = "selected_food_res"
        private const val KEY_SELECTED_FOOD_DATE = "selected_food_date"
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

    /**
     * 음식이 마지막으로 선택된 날짜가 오늘이 아니면(=날짜가 바뀌었으면) true.
     * 목표 전환 여부와는 무관하게, 오직 날짜 변경 시에만 음식을 재선택하기 위한 판단 기준.
     */
    fun isFoodOutdated(): Boolean {
        val savedDate = prefs.getString(KEY_SELECTED_FOOD_DATE, null)
        val today = LocalDate.now().toString()
        return savedDate != today
    }

    fun getSelectedFoodRes(): Int? =
        if (prefs.contains(KEY_SELECTED_FOOD_RES)) prefs.getInt(KEY_SELECTED_FOOD_RES, -1)
        else null

    fun saveTodayFood(foodRes: Int) {
        prefs.edit()
            .putString(KEY_SELECTED_FOOD_DATE, LocalDate.now().toString())
            .putInt(KEY_SELECTED_FOOD_RES, foodRes)
            .apply()
    }

}
