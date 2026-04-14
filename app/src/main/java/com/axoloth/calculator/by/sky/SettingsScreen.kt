package com.axoloth.calculator.by.sky

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



    fun render(parent: ViewGroup? = null): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.ui_settings, parent, false)

        btnPrivacy = view.findViewById(R.id.btnPrivacy)
        btnBacks = view.findViewById(R.id.btnBacks)
        btnGithub = view.findViewById(R.id.btnGithub)
        btnApkPure = view.findViewById(R.id.btnApkPure)

        setupLogic(view)
        return view
    }
    
    fun setupLogic(rootView: View) {
        btnBacks.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnBacks.startAnimation(anim)

            // 1. Siapkan view tujuan
            val kalkulatorView = KalkulatorScreen(activity).render()

            // 2. Ambil root container dari Activity (induk dari semua layout)
            val root = activity.findViewById<android.view.ViewGroup>(android.R.id.content)

            // 3. Gunakan TransitionManager (bukan TransitionSet)
            // Gravity.START membuat layar baru seolah muncul dari kiri (efek "Back")
            android.transition.TransitionManager.beginDelayedTransition(
                root,
                android.transition.Slide(android.view.Gravity.START)
            )

            // 4. Ganti kontennya
            activity.setContentView(kalkulatorView)
        }
        
        btnPrivacy.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnPrivacy.startAnimation(anim)
            val url = "https://docs.google.com/document/d/1VmUtMaq-CMV-T8Q0-fsqaC4Zyb-Ryh39qMB3i_ng938/edit?usp=sharing"
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, url.toUri())
        }

        btnGithub.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnGithub.startAnimation(anim)
            val url = "https://github.com/AMillionDriver"
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, url.toUri())
        }
        btnApkPure.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnApkPure.startAnimation(anim)
            val url = "https://apkpure.com/developer?id=30778344"
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, url.toUri())
        }
    }
}