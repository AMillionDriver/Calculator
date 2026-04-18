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
                if (step.startsWith("✓") || step.startsWith("Hasil")) {
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
    val cleaned = expression.replace("×", "*").replace("÷", "/")
    
    // Tampilkan Ekspresi Awal
    if (mode == "Complex") {
        steps.add("Ekspresinya:\n$expression\n")
        steps.add("Karena perkalian, pembagian, dan persen dikerjakan dulu, maka:")
    }

    // Step 1: Identifikasi "Terms"
    // Gunakan Regex untuk memisahkan tapi tetap menyimpan operatornya
    val tokens = cleaned.split(Regex("(?=[+-])|(?<=[+-])")).filter { it.isNotBlank() }
    val results = mutableListOf<Double>()
    val processedTerms = mutableListOf<String>()

    var stepCounter = 1
    tokens.forEach { token ->
        if (token == "+" || token == "-") {
            processedTerms.add(token)
        } else {
            // Hitung setiap blok (Term)
            val termResult = evaluateTerm(token)
            
            if (token.contains("*") || token.contains("/") || token.contains("%") || token.any { it.isLetter() }) {
                if (mode == "Complex") {
                    steps.add("${stepCounter++}) Hitung $token")
                    
                    // Detail khusus persen
                    if (token.contains("%") && !token.any { it.isLetter() }) {
                        val numPart = token.replace("%", "")
                        try {
                            val num = numPart.split("*", "/").last().toDoubleOrNull() ?: 0.0
                            steps.add("   $num% = ${num/100}")
                        } catch(e: Exception) {}
                    }
                    
                    steps.add("   $token = ${formatNum(termResult)}")
                }
            }
            results.add(if (processedTerms.lastOrNull() == "-") -termResult else termResult)
            processedTerms.add(formatNum(termResult))
        }
    }

    // Step 2: Penjumlahan Akhir
    if (mode == "Complex") {
        steps.add("\n${stepCounter++}) Jumlahkan semuanya:")
        steps.add(processedTerms.joinToString(" "))
        
        var runningTotal = results[0]
        for (i in 1 until results.size) {
            val nextVal = results[i]
            val op = if (nextVal >= 0) "+" else "-"
            val oldTotal = runningTotal
            runningTotal += nextVal
            steps.add("   ${formatNum(oldTotal)} $op ${formatNum(Math.abs(nextVal))} = ${formatNum(runningTotal)}")
        }
    } else if (mode == "Simple") {
        steps.add("Langkah Sederhana:")
        steps.add("= " + processedTerms.joinToString(" "))
        val finalRes = results.sum()
        steps.add("= ${formatNum(finalRes)}")
    }

    // Shortway: Strategi Mental Math (Shortcut)
    if (mode == "Shortway") {
        steps.add("Cara Cepat (Mental Math Strategy):")
        
        val termStrs = tokens.filter { it != "+" && it != "-" }
        val largeIdx = results.indices.maxByOrNull { Math.abs(results[it]) } ?: 0
        val largeVal = results[largeIdx]
        val largeStr = termStrs[largeIdx]
        
        // 1. Tampilkan Trik Distributif (khusus perkalian angka 99...)
        if (largeStr.contains("*")) {
            val parts = largeStr.split("*")
            val nStr = parts[0].trim()
            val mStr = parts[1].trim()
            
            if (nStr.all { it == '9' } && nStr.length >= 1) {
                val target = Math.pow(10.0, nStr.length.toDouble()).toLong()
                val mVal = evaluateTerm(mStr)
                steps.add("• $largeStr = ($target - 1) × $mStr")
                steps.add("  $largeStr = ${formatNum(target * mVal)} - ${formatNum(mVal)} = ${formatNum(largeVal)}")
            } else if (mStr.all { it == '9' } && mStr.length >= 1) {
                val target = Math.pow(10.0, mStr.length.toDouble()).toLong()
                val nVal = evaluateTerm(nStr)
                steps.add("• $largeStr = $nStr × ($target - 1)")
                steps.add("  $largeStr = ${formatNum(nVal * target)} - ${formatNum(nVal)} = ${formatNum(largeVal)}")
            } else {
                steps.add("• $largeStr = ${formatNum(largeVal)}")
            }
        } else {
            steps.add("• $largeStr = ${formatNum(largeVal)}")
        }

        // 2. Hitung sisa yang kecil
        steps.add("\nLalu sisanya:")
        val smallResults = mutableListOf<Double>()
        for (i in termStrs.indices) {
            if (i == largeIdx) continue
            val s = termStrs[i]
            val v = results[i]
            steps.add("  $s = ${formatNum(v)}")
            smallResults.add(v)
        }
        
        // 3. Gabungkan angka kecil
        val smallSum = smallResults.sum()
        if (smallResults.size > 1) {
            steps.add("\nGabungkan cepat angka kecil:")
            steps.add("  ${formatNum(smallSum)}")
        }
        
        // 4. Final Gabungan
        steps.add("\nTerakhir:")
        steps.add("  ${formatNum(largeVal)} + ${formatNum(smallSum)} = ${formatNum(largeVal + smallSum)}")
        
        steps.add("\nInti Shortway:")
        steps.add("• Ubah angka sulit (999...) jadi (1000 - 1)")
        steps.add("• Gabungkan angka kecil dulu")
        steps.add("• Baru tambah ke angka besar")
        steps.add("✓ ${formatNum(largeVal + smallSum)}")
        
        return steps
    } else {
        steps.add("\nHasil akhir:\n${formatNum(results.sum())}")
    }

    return steps
}

private fun evaluateTerm(term: String): Double {
    return try {
        var expr = term.replace("%", "/100")
            .replace("sin", "sin") // exp4j mendukung sin, cos, dll
            .replace("×", "*")
            .replace("÷", "/")
        
        // Cek jika term mengandung angka diikuti fungsi tanpa operator (misal 3sin)
        // Tambahkan perkalian otomatis jika perlu
        expr = expr.replace(Regex("(\\d)([a-z])"), "$1*$2")
        
        ExpressionBuilder(expr).build().evaluate()
    } catch (e: Exception) { 
        0.0 
    }
}

private fun formatNum(num: Double): String {
    return if (num % 1 == 0.0) num.toLong().toString() else "%.2f".format(Locale.US, num).trimEnd('0').trimEnd('.')
}
