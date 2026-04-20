package com.axoloth.calculator.by.sky.logic

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.axoloth.calculator.by.sky.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup
import net.objecthunter.exp4j.ExpressionBuilder
import java.math.BigDecimal
import java.util.*

fun showCalculationSteps(context: Context, expression: String) {
    val dialog = BottomSheetDialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.layout_explanation, null)
    dialog.setContentView(view)

    val tvTitle = view.findViewById<TextView>(R.id.tv_expl_title)
    val container = view.findViewById<LinearLayout>(R.id.steps_container)
    val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggle_mode)
    val btnClose = view.findViewById<Button>(R.id.btn_close_expl)

    tvTitle.text = "${context.getString(R.string.expl_title)}: $expression"

    btnClose.setOnClickListener {
        dialog.dismiss()
    }

    fun renderSteps(mode: String) {
        container.removeAllViews()
        val steps = generateSteps(expression, mode)
        steps.forEachIndexed { index, step ->
            val textView = TextView(context).apply {
                text = step
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setPadding(0, 12, 0, 12)
                setTextColor(context.getColor(android.R.color.white))
                
                // Styling khusus untuk step utama
                if (step.startsWith("Step") || step.startsWith("Hasil") || step.startsWith("Penyelesaian")) {
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(context.getColor(android.R.color.holo_blue_light))
                }
            }
            container.addView(textView)
            
            // Tambahkan garis pemisah antar langkah jika bukan langkah terakhir
            if (index < steps.size - 1) {
                val divider = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor(context.getColor(android.R.color.darker_gray))
                    alpha = 0.2f
                }
                container.addView(divider)
            }
        }
    }

    renderSteps("Complex") // Default ke mode Complex agar step-by-step terlihat

    toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
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
    val PI_VAL = "3.1415926535897932384626433832795028841971"
    
    // Normalisasi awal
    var currentExpr = expression.replace("×", "*").replace("÷", "/")
    steps.add("Soal Asli:\n$expression")

    if (mode == "Shortway") {
        steps.add("Trik Cepat:")
        steps.add("• Selesaikan perkalian besar dulu.")
        steps.add("• Gabungkan sisa unit di akhir.")
        val res = evaluateTerm(currentExpr)
        steps.add("\n✓ Hasil Akhir: ${formatNum(res)}")
        return steps
    }

    // --- LANGKAH 1: HANDLING PI & CONSTANTS ---
    if (currentExpr.contains("π")) {
        steps.add("Step 1) Substitusi nilai π (40 digit):")
        currentExpr = currentExpr.replace("π", "($PI_VAL)")
        steps.add("= $currentExpr")
    }

    // --- LANGKAH 2: PARENTHESES (KURUNG) ---
    if (currentExpr.contains("(")) {
        steps.add("Step 2) Hitung bagian di dalam kurung:")
        val parenthesesRegex = Regex("\\(([^()]+)\\)")
        var match = parenthesesRegex.find(currentExpr)
        while (match != null) {
            val inside = match.groupValues[1]
            val result = evaluateTerm(inside)
            val oldExpr = "($inside)"
            currentExpr = currentExpr.replace(oldExpr, formatNum(result))
            steps.add("→ $oldExpr menjadi ${formatNum(result)}")
            steps.add("= $currentExpr")
            match = parenthesesRegex.find(currentExpr)
        }
    }

    // --- LANGKAH 3: MULTIPLICATION / DIVISION (KALI / BAGI) ---
    if (currentExpr.contains("*") || currentExpr.contains("/")) {
        steps.add("Step 3) Hitung Perkalian dan Pembagian:")
        // Kita gunakan pendekatan blok untuk menyederhanakan visual
        val tokens = currentExpr.split(Regex("(?=[+-])|(?<=[+-])")).filter { it.isNotBlank() }
        val sb = StringBuilder()
        tokens.forEach { token ->
            if (token.contains("*") || token.contains("/")) {
                val res = evaluateTerm(token)
                sb.append(formatNum(res))
                steps.add("→ $token = ${formatNum(res)}")
            } else {
                sb.append(token)
            }
        }
        currentExpr = sb.toString()
        steps.add("= $currentExpr")
    }

    // --- LANGKAH 4: ADDITION / SUBTRACTION (TAMBAH / KURANG) ---
    if (currentExpr.contains("+") || (currentExpr.contains("-") && currentExpr.indexOf("-") > 0)) {
        steps.add("Step 4) Selesaikan Penjumlahan dan Pengurangan:")
        val finalResult = evaluateTerm(currentExpr)
        steps.add("= ${formatNum(finalResult)}")
    }

    // --- FINAL RESULT ---
    val finalRes = evaluateTerm(expression.replace("×", "*").replace("÷", "/"))
    steps.add("\nHasil Akhir:\n${formatNum(finalRes)}")

    return if (mode == "Simple") {
        listOf("Penyelesaian Sederhana:", expression, "= ${formatNum(finalRes)}")
    } else {
        steps
    }
}

private fun evaluateTerm(term: String): Double {
    return try {
        val PI_VAL = "3.1415926535897932384626433832795028841971"
        var expr = term.replace("π", "($PI_VAL)")
            .replace("e", "(2.7182818284)")
            .replace("%", "/100")
            .replace(",", ".") // Pastikan titik untuk engine
        
        // Implicit Multiply
        expr = expr.replace(Regex("(\\d)([a-zA-Z(])"), "$1*$2")
        
        ExpressionBuilder(expr).build().evaluate()
    } catch (e: Exception) { 0.0 }
}

private fun formatNum(num: Double): String {
    val locale = Locale.getDefault()
    return if (num % 1 == 0.0) {
        String.format(locale, "%d", num.toLong())
    } else {
        String.format(locale, "%.8f", num).trimEnd('0').trimEnd { it == '.' || it == ',' }
    }
}
