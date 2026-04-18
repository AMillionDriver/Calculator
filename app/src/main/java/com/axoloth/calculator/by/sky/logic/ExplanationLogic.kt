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

    tvTitle.text = "Cara Menghitung: $expression"

    btnClose.setOnClickListener {
        dialog.dismiss()
    }

    fun renderSteps(mode: String) {
        container.removeAllViews()
        val steps = generateSteps(expression, mode)
        steps.forEach { step ->
            val textView = TextView(context).apply {
                text = step
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setPadding(0, 16, 0, 16)
                setTextColor(context.getColor(android.R.color.white))
                if (step.startsWith("✓") || step.startsWith("Hasil") || step.startsWith("Step")) {
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
            }
            container.addView(textView)
        }
    }

    renderSteps("Simple")

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
    
    // Tampilkan Informasi Konstanta jika ada
    if (expression.contains("π") && mode == "Complex") {
        steps.add("Catatan: π yang digunakan adalah High Precision (40 digit):\n$PI_VAL\n")
    }

    // Pembersihan Dasar
    val cleaned = expression.replace("×", "*").replace("÷", "/")
    
    if (mode == "Complex") {
        steps.add("Aturan BODMAS/PEMDAS:")
        steps.add("1. Kurung ()\n2. Pangkat ^\n3. Perkalian/Pembagian\n4. Penjumlahan/Pengurangan\n")
    }

    // Step 1: Handling Parentheses (Langkah di dalam kurung)
    var currentExpr = cleaned
    if (currentExpr.contains("(") && mode == "Complex") {
        steps.add("Step 1) Selesaikan di dalam kurung:")
        val parenthesesRegex = Regex("\\(([^()]+)\\)")
        var match = parenthesesRegex.find(currentExpr)
        while (match != null) {
            val inside = match.groupValues[1]
            val result = evaluateTerm(inside)
            steps.add("   ($inside) = ${formatNum(result)}")
            currentExpr = currentExpr.replace("($inside)", formatNum(result))
            match = parenthesesRegex.find(currentExpr)
        }
        steps.add("Ekspresi sekarang: $currentExpr\n")
    }

    // Step 2: Split by Terms (Penjumlahan/Pengurangan)
    val tokens = currentExpr.split(Regex("(?=[+-])|(?<=[+-])")).filter { it.isNotBlank() }
    val results = mutableListOf<Double>()
    val processedTerms = mutableListOf<String>()

    var stepIdx = if (mode == "Complex") 2 else 1
    tokens.forEach { token ->
        if (token == "+" || token == "-") {
            processedTerms.add(token)
        } else {
            val termResult = evaluateTerm(token)
            
            if (mode == "Complex" && (token.contains("*") || token.contains("/") || token.contains("%") || token.contains("^"))) {
                steps.add("Step $stepIdx) Hitung Blok: $token")
                
                // Penjelasan Khusus Persen
                if (token.contains("%")) {
                    steps.add("   Ingat: % berarti dibagi 100")
                }
                
                steps.add("   Hasil = ${formatNum(termResult)}")
                stepIdx++
            }
            
            results.add(if (processedTerms.lastOrNull() == "-") -termResult else termResult)
            processedTerms.add(formatNum(termResult))
        }
    }

    // Final Assembly
    if (mode == "Complex") {
        steps.add("\nStep $stepIdx) Gabungkan hasil akhir:")
        steps.add(processedTerms.joinToString(" "))
        
        if (results.size > 1) {
            var runningTotal = results[0]
            for (i in 1 until results.size) {
                val nextVal = results[i]
                val op = if (nextVal >= 0) "+" else "-"
                val oldTotal = runningTotal
                runningTotal += nextVal
                steps.add("   ${formatNum(oldTotal)} $op ${formatNum(Math.abs(nextVal))} = ${formatNum(runningTotal)}")
            }
        }
    } else if (mode == "Simple") {
        steps.add("Penyelesaian Sederhana:")
        steps.add("= " + processedTerms.joinToString(" "))
        steps.add("= ${formatNum(results.sum())}")
    }

    // Shortway Strategy
    if (mode == "Shortway") {
        steps.add("Trik Cepat (Mental Math):")
        val finalRes = results.sum()
        
        if (expression.contains("9")) {
            steps.add("• Gunakan pembulatan (misal 99 jadi 100-1)")
        }
        steps.add("• Hitung angka besar terlebih dahulu")
        steps.add("• Gabungkan sisa angka kecil di akhir")
        steps.add("\n✓ Hasil Akhir: ${formatNum(finalRes)}")
        return steps
    }

    steps.add("\nHasil Akhir:\n${formatNum(results.sum())}")
    return steps
}

private fun evaluateTerm(term: String): Double {
    return try {
        val PI_VAL = "3.1415926535897932384626433832795028841971"
        var expr = term.replace("π", "($PI_VAL)")
            .replace("e", "(2.7182818284)")
            .replace("%", "/100")
        
        // Implicit Multiply
        expr = expr.replace(Regex("(\\d)([a-zA-Z(])"), "$1*$2")
        
        ExpressionBuilder(expr).build().evaluate()
    } catch (e: Exception) { 0.0 }
}

private fun formatNum(num: Double): String {
    return if (num % 1 == 0.0) num.toLong().toString() else "%.8f".format(Locale.US, num).trimEnd('0').trimEnd('.')
}
