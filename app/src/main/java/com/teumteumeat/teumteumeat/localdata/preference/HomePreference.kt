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
        private const val KEY_COUPON_ACTIVE_GOAL_ID = "coupon_active_goal_id"
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

    /**
     * 쿠폰 사용으로 임시 활성화(Available)된 목표 id를 저장합니다.
     * 요약글 조회 후 퀴즈를 풀기 전 홈으로 돌아와도 이 목표에 한해 활성화 상태를 유지하기 위함입니다.
     */
    fun saveCouponActiveGoalId(goalId: Long) {
        prefs.edit()
            .putLong(KEY_COUPON_ACTIVE_GOAL_ID, goalId)
            .apply()
    }

    /**
     * 쿠폰으로 활성화된 목표 id. 저장된 적 없으면 null.
     */
    fun getCouponActiveGoalId(): Long? =
        if (prefs.contains(KEY_COUPON_ACTIVE_GOAL_ID)) prefs.getLong(KEY_COUPON_ACTIVE_GOAL_ID, -1L)
        else null

    /**
     * 쿠폰 활성화 상태를 해제합니다. (목표 변경 감지 시, 또는 퀴즈 완료 시 호출)
     */
    fun clearCouponActiveGoalId() {
        prefs.edit()
            .remove(KEY_COUPON_ACTIVE_GOAL_ID)
            .apply()
    }

}
