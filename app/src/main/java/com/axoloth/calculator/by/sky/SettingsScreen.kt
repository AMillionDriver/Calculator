package com.axoloth.calculator.by.sky

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

class SettingsScreen (val activity: AppCompatActivity) {
    private lateinit var btnPrivacy: Button
    private lateinit var btnBacks: Button
    private lateinit var btnGithub: Button
    private lateinit var btnApkPure: Button

    companion object {
        private const val TAG = "SettingsScreen"
    }

    fun render(parent: ViewGroup? = null): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.ui_settings, parent, false)

        btnPrivacy = view.findViewById(R.id.btnPrivacy)
        btnBacks = view.findViewById(R.id.btnBacks)
        btnGithub = view.findViewById(R.id.btnGithub)
        btnApkPure = view.findViewById(R.id.btnApkPure)

        setupLogic()
        return view
    }
    
    fun setupLogic() {
        // Logika Tombol Kembali
        btnBacks.setOnClickListener {
            try {
                val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
                btnBacks.startAnimation(anim)

                val kalkulatorView = KalkulatorScreen(activity).render()
                val root = activity.findViewById<ViewGroup>(android.R.id.content)

                if (root != null) {
                    android.transition.TransitionManager.beginDelayedTransition(
                        root,
                        android.transition.Slide(android.view.Gravity.START)
                    )
                    activity.setContentView(kalkulatorView)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during navigation back: ${e.message}")
                // Fallback jika transisi gagal
                activity.setContentView(KalkulatorScreen(activity).render())
            }
        }
        
        // Logika Buka URL (Privacy, Github, ApkPure)
        btnPrivacy.setOnClickListener { openUrl("https://docs.google.com/document/d/1VmUtMaq-CMV-T8Q0-fsqaC4Zyb-Ryh39qMB3i_ng938/edit?usp=sharing", btnPrivacy) }
        btnGithub.setOnClickListener { openUrl("https://github.com/AMillionDriver", btnGithub) }
        btnApkPure.setOnClickListener { openUrl("https://apkpure.com/developer?id=30778344", btnApkPure) }
    }

    /**
     * Membuka URL dengan Custom Tabs secara aman
     */
    private fun openUrl(url: String, button: Button) {
        try {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            button.startAnimation(anim)
            
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, url.toUri())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open URL: $url", e)
            // Fallback: Bisa tampilkan Toast atau gunakan Intent biasa jika browser tidak mendukung Custom Tabs
        }
    }
}