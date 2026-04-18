package com.axoloth.calculator.by.sky.screen

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.logic.setupKursLogic

/**
 * Merender layar Konversi Mata Uang (Kurs).
 */
fun renderKursScreen(activity: AppCompatActivity, parent: ViewGroup? = null): View {
    val view = activity.layoutInflater.inflate(R.layout.ui_kurs, parent, false)
    
    // Inisialisasi Logika
    setupKursLogic(activity, view)
    
    return view
}
