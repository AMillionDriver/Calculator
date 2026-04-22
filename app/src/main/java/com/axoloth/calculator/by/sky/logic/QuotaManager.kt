package com.axoloth.calculator.by.sky.logic

import android.content.Context
import android.content.SharedPreferences

object QuotaManager {
    private const val PREF_NAME = "ai_quota_prefs"
    private const val KEY_QUOTA = "remaining_quota"
    private const val KEY_ADS_WATCHED = "ads_watched_count"
    private const val KEY_COOLDOWN_TIME = "cooldown_timestamp"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Ambil sisa kuota (Default 5)
    fun getRemainingQuota(context: Context): Int {
        return getPrefs(context).getInt(KEY_QUOTA, 5)
    }

    // Kurangi kuota saat pakai AI
    fun decrementQuota(context: Context) {
        val current = getRemainingQuota(context)
        if (current > 0) {
            getPrefs(context).edit().putInt(KEY_QUOTA, current - 1).apply()
        }
    }

    // Tambah kuota setelah nonton iklan (+5)
    fun addQuota(context: Context) {
        val current = getRemainingQuota(context)
        val currentAds = getAdsWatchedToday(context)
        
        getPrefs(context).edit()
            .putInt(KEY_QUOTA, current + 5)
            .putInt(KEY_ADS_WATCHED, currentAds + 1)
            .putLong(KEY_COOLDOWN_TIME, System.currentTimeMillis())
            .apply()
    }

    // Cek sudah berapa kali nonton iklan
    fun getAdsWatchedToday(context: Context): Int {
        val prefs = getPrefs(context)
        val lastTime = prefs.getLong(KEY_COOLDOWN_TIME, 0L)
        
        // Jika sudah lebih dari 30 menit, reset hitungan iklan
        if (System.currentTimeMillis() - lastTime > 30 * 60 * 1000) {
            prefs.edit().putInt(KEY_ADS_WATCHED, 0).apply()
            return 0
        }
        return prefs.getInt(KEY_ADS_WATCHED, 0)
    }

    // Cek apakah sedang masa cool-down (setelah 3 iklan)
    fun isCoolDown(context: Context): Boolean {
        val watched = getAdsWatchedToday(context)
        if (watched >= 3) {
            val lastTime = getPrefs(context).getLong(KEY_COOLDOWN_TIME, 0L)
            val diff = System.currentTimeMillis() - lastTime
            return diff < 30 * 60 * 1000 // Masih dalam 30 menit
        }
        return false
    }

    // Hitung sisa waktu cooldown dalam menit
    fun getRemainingCooldownMinutes(context: Context): Int {
        val lastTime = getPrefs(context).getLong(KEY_COOLDOWN_TIME, 0L)
        val remainingMillis = (30 * 60 * 1000) - (System.currentTimeMillis() - lastTime)
        return (remainingMillis / (60 * 1000)).toInt().coerceAtLeast(0)
    }
}
