package com.axoloth.calculator.by.sky.logic

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.axoloth.calculator.by.sky.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup

fun showCalculationSteps(context: Context, expression: String) {
    val dialog = BottomSheetDialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.layout_explanation, null)
    dialog.setContentView(view)

    val tvTitle = view.findViewById<TextView>(R.id.tv_expl_title)
    val container = view.findViewById<LinearLayout>(R.id.steps_container)
    val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggle_mode)

    tvTitle.text = "Cara Menghitung: $expression"

    // Fungsi untuk merender langkah berdasarkan mode
    fun renderSteps(mode: String) {
        container.removeAllViews()
        val steps = generateSteps(expression, mode)
        steps.forEach { step ->
            val textView = TextView(context).apply {
                text = step
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setPadding(0, 24, 0, 24)
                setTextColor(context.getColor(android.R.color.white))
            }
            container.addView(textView)
        }
    }

    // Default: Simple Mode
    renderSteps("Simple")

    toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
        if (isChecked) {
            when (checkedId) {
                R.id.btn_mode_complex -> renderSteps("Complex")
                R.id.btn_mode_simple -> renderSteps("Simple")
                R.id.btn_mode_short -> renderSteps("Shortway")
            }
        }
    }

    dialog.show()
}

private fun generateSteps(expression: String, mode: String): List<String> {
    val steps = mutableListOf<String>()
    
    // Logika simulasi pemecahan (Sederhana untuk demo)
    // Di dunia nyata, ini akan menggunakan parser yang mengevaluasi per node
    
    when (mode) {
        "Complex" -> {
            steps.add("1. Identifikasi Ekspresi: Kita punya $expression")
            if (expression.contains("%")) steps.add("2. Persen: Ubah nilai persen menjadi pembagian 100.")
            steps.add("3. Urutan Operasi: Lakukan perkalian dan pembagian dari kiri ke kanan.")
            steps.add("4. Hasil Akhir: Evaluasi total.")
        }
        "Simple" -> {
            steps.add(expression)
            var current = expression.replace("%", "/100")
            steps.add("= $current")
            // Simulasi hasil antara
            steps.add("= ... perhitungan antara ...")
        }
        "Shortway" -> {
            steps.add("Rumus: $expression")
            steps.add("Langsung: Hitung semua operator sekaligus.")
        }
    }
    
    return steps
}

// Extension property untuk mempermudah padding
private val Int.sp: Float get() = this.toFloat()
