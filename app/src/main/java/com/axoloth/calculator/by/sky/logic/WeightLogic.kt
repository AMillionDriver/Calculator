package com.axoloth.calculator.by.sky.logic

import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.screen.renderKalkulatorScreen
import com.google.android.material.snackbar.Snackbar

/**
 * Mengatur logika untuk layar Konversi Satuan (Weight/Unit Screen).
 */
fun setupWeightLogic(activity: AppCompatActivity, view: View) {
    val btnBack: Button = view.findViewById(R.id.btnBack)
    val btnPanjang: Button = view.findViewById(R.id.btnPanjang)
    val btnArea: Button = view.findViewById(R.id.btnArea)
    val btnVolume: Button = view.findViewById(R.id.btnVolume)
    val btnKecepatan: Button = view.findViewById(R.id.btnKecepatan)
    val btnBerat: Button = view.findViewById(R.id.btnBerat)
    val btnSuhu: Button = view.findViewById(R.id.btnSuhu)
    val btnDaya: Button = view.findViewById(R.id.btnDaya)
    val btnTekanan: Button = view.findViewById(R.id.btnTekanan)

    // Logika Tombol Kembali
    btnBack.setOnClickListener {
        playAnim(activity, it)
        val root = activity.findViewById<ViewGroup>(android.R.id.content)
        TransitionManager.beginDelayedTransition(root, Slide(Gravity.START))
        activity.setContentView(renderKalkulatorScreen(activity))
    }

    // List tombol kategori
    val categoryButtons = listOf(
        btnPanjang, btnArea, btnVolume, btnKecepatan, btnBerat, btnSuhu, btnDaya, btnTekanan
    )

    categoryButtons.forEach { btn ->
        btn.setOnClickListener {
            playAnim(activity, it)
            // Contoh pesan untuk fitur yang belum diimplementasikan
            val categoryName = (it as Button).text
            Snackbar.make(view, "Fitur Konversi $categoryName Segera Hadir!", Snackbar.LENGTH_SHORT).show()
        }
    }
}

private fun playAnim(activity: AppCompatActivity, view: View) {
    val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
    view.startAnimation(anim)
}
