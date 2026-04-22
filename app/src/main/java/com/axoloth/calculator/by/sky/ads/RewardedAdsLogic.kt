package com.axoloth.calculator.by.sky.ads

import android.app.Activity
import android.util.Log
import com.axoloth.calculator.by.sky.ads.remote.AdsRemoteConfig
import com.axoloth.calculator.by.sky.logic.QuotaManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardedAdsLogic {
    private const val TAG = "RewardedAdsLogic"
    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    // Load iklan di background
    fun loadRewardedAd(activity: Activity) {
        if (!AdsRemoteConfig.isRewardedAdEnabled()) {
            Log.d(TAG, "Rewarded Ads are disabled via Remote Config")
            return
        }
        if (isAdLoading || rewardedAd != null) return
        if (QuotaManager.isCoolDown(activity)) {
            Log.d(TAG, "Skipping load: Still in cooldown")
            return
        }

        isAdLoading = true
        val adUnitId = AdsRemoteConfig.getRewardedAdUnitId()
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(activity, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Rewarded Ad failed to load: ${adError.message}")
                rewardedAd = null
                isAdLoading = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Rewarded Ad loaded successfully!")
                rewardedAd = ad
                isAdLoading = false
            }
        })
    }

    // Tampilkan iklan
    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                // USER BERHASIL NONTON SAMPAI HABIS
                val rewardAmount = rewardItem.amount
                Log.d(TAG, "User earned reward: $rewardAmount")
                
                QuotaManager.addQuota(activity)
                onRewardEarned()
                
                // Hapus iklan yang sudah dipakai & load yang baru jika belum cooldown
                rewardedAd = null
                loadRewardedAd(activity)
            }
        } else {
            Log.d(TAG, "Rewarded Ad not ready yet")
            loadRewardedAd(activity)
        }
    }
    
    fun isAdReady(): Boolean = rewardedAd != null
}
