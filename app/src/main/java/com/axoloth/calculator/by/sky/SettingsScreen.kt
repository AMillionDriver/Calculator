package com.axoloth.calculator.by.sky

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar

class SettingsScreen (val context: Context) {
    private lateinit var btnAccount: Button
    private lateinit var btnPrivacy: Button
    private lateinit var btnBacks: Button

    fun render(parent: ViewGroup? = null): View {
        val view = LayoutInflater.from(context).inflate(R.layout.ui_settings, parent, false)

        btnAccount =view.findViewById(R.id.btnAccount)
        btnPrivacy =view.findViewById(R.id.btnPrivacy)
        btnBacks =view.findViewById(R.id.btnBacks)

        setupLogic(view)
        return view
    }
    fun setupLogic(rootView: View) {
        btnBacks.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.button_click)
            btnBacks.startAnimation(anim)
            val intent = Intent(context, KalkulatorScreen::class.java)
            context.startActivity(intent)
        }
        btnAccount.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.button_click)
            btnAccount.startAnimation(anim)
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show(
            )
        }
        btnPrivacy.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.button_click)
            btnPrivacy.startAnimation(anim)
            val url = "https://docs.google.com/document/d/1VmUtMaq-CMV-T8Q0-fsqaC4Zyb-Ryh39qMB3i_ng938/edit?usp=sharing"
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, url.toUri())
        }
    }
}