package com.teumteumeat.teumteumeat.utils.manager.ad

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // 광고 객체를 StateFlow로 관리 (UI나 ViewModel에서 관찰 가능)
    private val _interstitialAd = MutableStateFlow<InterstitialAd?>(null)
    val interstitialAd: StateFlow<InterstitialAd?> = _interstitialAd.asStateFlow()

    private var isLoading = false

    // 광고 로드 함수
    fun loadAd() {
        if (isLoading || _interstitialAd.value != null) return // 이미 로드 중이거나 광고가 있으면 스킵

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        val adId = if(BuildConfig.DEBUG) context.getString(R.string.fullscreen_ad_test_id)
            else context.getString(R.string.fullscreen_ad_id)

        InterstitialAd.load(context, adId, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isLoading = false
                    _interstitialAd.value = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    isLoading = false
                    _interstitialAd.value = interstitialAd
                }
            })
    }

    // 광고 노출 후 상태 초기화
    fun clearAd() {
        _interstitialAd.value = null
    }
}