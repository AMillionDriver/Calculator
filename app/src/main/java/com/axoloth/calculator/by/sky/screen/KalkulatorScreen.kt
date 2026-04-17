package com.axoloth.calculator.by.sky.screen

import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.axoloth.calculator.by.sky.R

/**
 * Fungsi utama untuk merender tampilan kalkulator.
 * Tidak menggunakan class agar sesuai dengan keinginan user (Single Activity).
 */
fun renderKalkulatorScreen(activity: AppCompatActivity, parent: ViewGroup? = null): View {
    val view = activity.layoutInflater.inflate(R.layout.ui_kalkulator, parent, false)

    val tvInput: EditText = view.findViewById(R.id.tv_input)
    val tvResult: TextView = view.findViewById(R.id.tv_result)

    // Pindahkan logika ke file Logic
    com.axoloth.calculator.by.sky.logic.setupKalkulatorLogic(activity, view, tvInput, tvResult)
    
    return view
}

private fun setupNumericButtons(activity: AppCompatActivity, view: View, tvInput: TextView) {
    val numericButtons = listOf(
        R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
        R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
        R.id.btn_nolnol, R.id.btn_koma
    )

    numericButtons.forEach { id ->
        view.findViewById<Button>(id).setOnClickListener { btn ->
            val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btn.startAnimation(anim)
            tvInput.append((btn as Button).text)
        }
    }
}

private fun setupNavigationButtons(activity: AppCompatActivity, view: View) {
    // Tombol Settings
    view.findViewById<Button>(R.id.btn_settings).setOnClickListener { btn ->
        val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
        btn.startAnimation(anim)
        
        // Pindah ke Settings
        navigateToSettings(activity)
    }
}

private fun navigateToSettings(activity: AppCompatActivity) {
    val root = activity.findViewById<ViewGroup>(android.R.id.content)
    
    // Animasi Slide
    TransitionManager.beginDelayedTransition(root, Slide(Gravity.END))
    
    // Ganti isi Activity dengan render dari SettingsScreen
    activity.setContentView(renderSettingsScreen(activity))
}