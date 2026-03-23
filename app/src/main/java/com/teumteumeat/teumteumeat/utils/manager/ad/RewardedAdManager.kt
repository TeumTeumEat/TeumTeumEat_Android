package com.teumteumeat.teumteumeat.utils.manager.ad

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // 광고 객체를 StateFlow로 관리 (UI나 ViewModel에서 관찰 가능)
    private val _rewardedAd = MutableStateFlow<RewardedAd?>(null)
    val rewardedAd: StateFlow<RewardedAd?> = _rewardedAd.asStateFlow()

    private var isLoading = false

    // 광고 로드 함수
    fun loadAd() {
        if (isLoading || _rewardedAd.value != null) return // 이미 로드 중이거나 광고가 있으면 스킵

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        val adId = if(BuildConfig.DEBUG) context.getString(R.string.rewarded_ad_test_id)
            else context.getString(R.string.rewarded_ad_id)

        RewardedAd.load(context, adId, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isLoading = false
                    _rewardedAd.value = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    isLoading = false
                    _rewardedAd.value = ad
                }
            })
    }

    // 광고 노출 후 상태 초기화
    fun clearAd() {
        _rewardedAd.value = null
    }
}