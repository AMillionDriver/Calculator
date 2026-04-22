package com.axoloth.calculator.by.sky.logic

import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.appcompat.app.AlertDialog
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.logic.LocaleHelper
import com.google.android.datatransport.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

private const val TAG = "SettingsLogic"

/**
 * Mengatur logika untuk layar Settings.
 */
fun setupSettingsLogic(activity: AppCompatActivity, view: View) {
    val btnBacks: Button = view.findViewById(R.id.btnBacks)
    val btnPrivacy: Button = view.findViewById(R.id.btnPrivacy)
    val btnGithub: Button = view.findViewById(R.id.btnGithub)
    val btnApkPure: Button = view.findViewById(R.id.btnApkPure)
    val btnLanguage: Button = view.findViewById(R.id.btnLangguage)

    // Logika Tombol Kembali
    btnBacks.setOnClickListener {
        playAnim(activity, it)
        activity.onBackPressedDispatcher.onBackPressed()
    }

    // Logika URL
    btnPrivacy.setOnClickListener { 
        openUrl(activity, it, "https://docs.google.com/document/d/1VmUtMaq-CMV-T8Q0-fsqaC4Zyb-Ryh39qMB3i_ng938/edit?usp=sharing") 
    }
    btnGithub.setOnClickListener { 
        openUrl(activity, it, "https://github.com/AMillionDriver") 
    }
    btnApkPure.setOnClickListener { 
        openUrl(activity, it, "https://apkpure.com/developer?id=30778344") 
    }

    // Logika Pilihan Bahasa
    btnLanguage.setOnClickListener {
        playAnim(activity, it)
        showLanguageDialog(activity)
    }

    // Inisialisasi Banner Ads dengan Smart Retry
    val adContainer: android.widget.FrameLayout = view.findViewById(R.id.adContainer)

    fun loadBanner() {
        if (!com.axoloth.calculator.by.sky.ads.remote.AdsRemoteConfig.isBannerAdEnabled()) {
            Log.d(TAG, "Banner Ads are disabled via Remote Config")
            adContainer.visibility = View.GONE
            return
        }

        try {
            val adView = com.google.android.gms.ads.AdView(activity)
            val adUnitId = com.axoloth.calculator.by.sky.ads.remote.AdsRemoteConfig.getBannerAdUnitId()
            
            adView.adUnitId = adUnitId
            adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER)
            
            adView.adListener = object : com.google.android.gms.ads.AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "Banner Ad loaded successfully!")
                    adContainer.removeAllViews()
                    adContainer.addView(adView)
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    Log.e(TAG, "Banner Ad failed: ${error.message}, Code: ${error.code}")
                    // Jika error 0 (Internal), coba lagi sekali setelah 3 detik
                    if (error.code == 0) {
                        Log.d(TAG, "Internal error detected, retrying in 3s...")
                    }
                }
            }

            val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.e(TAG, "AdView initialization failed", e)
        }
    }

    adContainer.postDelayed({ loadBanner() }, 1500)

    // Debug Crash Button
    if (BuildConfig.DEBUG) {
        val layout = view.findViewById<LinearLayout>(R.id.linearLayout2)
        layout?.let {
            val crashBtn = Button(activity).apply {
                text = "Test Crash (Debug)"
                setTextColor(android.graphics.Color.RED)
                setOnClickListener { throw RuntimeException("Manual Debug Crash") }
            }
            it.addView(crashBtn)
        }
    }
}

private fun openUrl(activity: AppCompatActivity, view: View, url: String) {
    try {
        playAnim(activity, view)
        CustomTabsIntent.Builder().build().launchUrl(activity, url.toUri())
    } catch (e: Exception) {
        Log.e(TAG, "Failed to open URL: $url", e)
    }
}

private fun showLanguageDialog(activity: AppCompatActivity) {
    val languages = arrayOf(
        "English (US)",
        "English (UK)",
        "Bahasa Indonesia",
        "العربية (Arabic - UAE)",
        "Deutsch (German)",
        "Español (Spanish)",
        "Français (French)",
        "Français (Swiss)",
        "Italiano (Italian)",
        "Italiano (Swiss)",
        "日本語 (Japanese)",
        "한국어 (Korean)",
        "Português (Brazil)",
        "Русский (Russian)",
        "中文 (Chinese)"
    )

    // Sesuai dengan nama folder values-xx-rXX
    val languageCodes = arrayOf(
        "en-US", "en-GB", "in-ID", "ar-AE", "de-DE", 
        "es-ES", "fr-FR", "fr-CH", "it-IT", "it-CH", 
        "ja-JP", "ko-KR", "pt-BR", "ru-RU", "zh-CN"
    )

    AlertDialog.Builder(activity, R.style.Theme_Kalkulator)
        .setTitle(activity.getString(R.string.langguage))
        .setItems(languages) { _, which ->
            val selectedTag = languageCodes[which]
            LocaleHelper.setLocale(activity, selectedTag)
            
            // Restart Activity untuk menerapkan bahasa
            activity.recreate() 
        }
        .show()
}

private fun playAnim(activity: AppCompatActivity, view: View) {
    val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
    view.startAnimation(anim)
}
