package com.axoloth.calculator.by.sky.screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.logic.setupWeightLogic

/**
 * Fungsi untuk merender tampilan konversi (Weight/Unit Converter).
 * Menggunakan pola Single Activity (Fungsi, bukan Class).
 */
fun renderWeightScreen(activity: AppCompatActivity, parent: ViewGroup? = null): View {
    val view = LayoutInflater.from(activity).inflate(R.layout.ui_weight, parent, false)

    // Hubungkan ke Logic
    setupWeightLogic(activity, view)
    
    return view
}
