package com.axoloth.calculator.by.sky.screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.logic.setupSettingsLogic

/**
 * Fungsi utama untuk merender tampilan Settings.
 * Mengikuti pola Single Activity (Fungsi, bukan Class).
 */
fun renderSettingsScreen(activity: AppCompatActivity, parent: ViewGroup? = null): View {
    val view = LayoutInflater.from(activity).inflate(R.layout.ui_settings, parent, false)

    // Hubungkan ke Logic
    setupSettingsLogic(activity, view)
    
    return view
}
