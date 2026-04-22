package com.axoloth.calculator.by.sky.ads.remote

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object AdsRemoteConfig {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        // Set default agar iklan AKTIF jika Firebase belum terkoneksi
        val defaults = mapOf(
            "ShowNativeAds" to true,
            "ShowBannerAds" to true,
            "ShowRewardedAds" to true,
            "NativeAds" to "",
            "BannerAdmob" to "",
            "RewardedAds" to ""
        )
        remoteConfig.setDefaultsAsync(defaults)
    }

    // Default Test IDs (Google)
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    /**
     * Cek apakah Native Ads diizinkan tampil.
     */
    fun isNativeAdEnabled(): Boolean {
        return remoteConfig.getBoolean("ShowNativeAds")
    }

    /**
     * Cek apakah Banner Ads diizinkan tampil.
     */
    fun isBannerAdEnabled(): Boolean {
        return remoteConfig.getBoolean("ShowBannerAds")
    }

    /**
     * Cek apakah Rewarded Ads diizinkan tampil.
     */
    fun isRewardedAdEnabled(): Boolean {
        return remoteConfig.getBoolean("ShowRewardedAds")
    }

    /**
     * Mengambil ID Banner dari Firebase Remote Config.
     */
    fun getBannerAdUnitId(): String {
        val remoteId = remoteConfig.getString("BannerAdmob")
        return if (remoteId.isNotEmpty()) remoteId else TEST_BANNER_ID
    }

    /**
     * Mengambil ID Native Ads dari Firebase Remote Config.
     */
    fun getNativeAdUnitId(): String {
        val remoteId = remoteConfig.getString("NativeAds")
        return if (remoteId.isNotEmpty()) remoteId else TEST_NATIVE_ID
    }

    /**
     * Mengambil ID Rewarded Ads dari Firebase Remote Config.
     */
    fun getRewardedAdUnitId(): String {
        val remoteId = remoteConfig.getString("RewardedAds")
        return if (remoteId.isNotEmpty()) remoteId else TEST_REWARDED_ID
    }
}
