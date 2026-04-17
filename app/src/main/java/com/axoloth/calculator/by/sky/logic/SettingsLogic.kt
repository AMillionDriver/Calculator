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
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.screen.renderKalkulatorScreen
import com.google.android.datatransport.BuildConfig

private const val TAG = "SettingsLogic"

/**
 * Mengatur logika untuk layar Settings.
 */
fun setupSettingsLogic(activity: AppCompatActivity, view: View) {
    val btnBacks: Button = view.findViewById(R.id.btnBacks)
    val btnPrivacy: Button = view.findViewById(R.id.btnPrivacy)
    val btnGithub: Button = view.findViewById(R.id.btnGithub)
    val btnApkPure: Button = view.findViewById(R.id.btnApkPure)

    // Logika Tombol Kembali
    btnBacks.setOnClickListener {
        try {
            playAnim(activity, it)
            val root = activity.findViewById<ViewGroup>(android.R.id.content)
            TransitionManager.beginDelayedTransition(root, Slide(Gravity.START))
            activity.setContentView(renderKalkulatorScreen(activity))
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating back", e)
            activity.setContentView(renderKalkulatorScreen(activity))
        }
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

private fun playAnim(activity: AppCompatActivity, view: View) {
    val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
    view.startAnimation(anim)
}
