package com.axoloth.calculator.by.sky.logic

import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.axoloth.calculator.by.sky.MainActivity
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.screen.renderSettingsScreen
import com.axoloth.calculator.by.sky.screen.renderWeightScreen
import com.google.android.material.snackbar.Snackbar
import net.objecthunter.exp4j.ExpressionBuilder

/**
 * Mengatur semua logika tombol untuk KalkulatorScreen dengan Cursor Editing.
 */
fun setupKalkulatorLogic(activity: AppCompatActivity, view: View, tvInput: EditText, tvResult: TextView) {
    val pref = activity.getSharedPreferences("kalkulator_prefs", Context.MODE_PRIVATE)

    // Matikan keyboard sistem tapi tetap aktifkan kursor
    tvInput.showSoftInputOnFocus = false
    tvInput.requestFocus()

    // Load state terakhir
    val lastInput = pref.getString("last_input", "") ?: ""
    if (lastInput.isNotEmpty()) {
        tvInput.setText(lastInput)
        tvInput.setSelection(tvInput.text.length)
        updatePreview(tvInput, tvResult)
    }

    // Fungsi simpan otomatis
    val saveInput = { text: String ->
        pref.edit().putString("last_input", text).apply()
    }

    // Helper untuk Cursor Input
    val insertText = { text: String ->
        val start = tvInput.selectionStart
        val end = tvInput.selectionEnd
        tvInput.text.replace(start, end, text)
        updatePreview(tvInput, tvResult)
        saveInput(tvInput.text.toString())
    }

    // Tombol Settings
    view.findViewById<Button>(R.id.btn_settings).setOnClickListener {
        playAnim(activity, it)
        val rootLayout = activity.findViewById<ViewGroup>(android.R.id.content)
        TransitionManager.beginDelayedTransition(rootLayout, Slide(Gravity.END))
        
        if (activity is MainActivity) activity.updateCurrentScreen("Settings")
        activity.setContentView(renderSettingsScreen(activity))
    }

    // Tombol Kurs (Coming Soon)
    view.findViewById<Button>(R.id.btn_kurs).setOnClickListener {
        playAnim(activity, it)
        Snackbar.make(view, "This Feature Will Available Soon", Snackbar.LENGTH_SHORT).show()
    }

    // Tombol Weight
    view.findViewById<Button>(R.id.btn_weight).setOnClickListener {
        playAnim(activity, it)
        val rootLayout = activity.findViewById<ViewGroup>(android.R.id.content)
        TransitionManager.beginDelayedTransition(rootLayout, Slide(Gravity.END))
        
        if (activity is MainActivity) activity.updateCurrentScreen("Weight")
        activity.setContentView(renderWeightScreen(activity))
    }

    // Tombol Clear (AC/C)
    view.findViewById<Button>(R.id.btn_c).setOnClickListener {
        playAnim(activity, it)
        tvInput.setText("")
        tvResult.text = "0"
        tvResult.alpha = 1.0f
        saveInput("")
    }

    // Tombol Backspace
    view.findViewById<Button>(R.id.btn_backspace).setOnClickListener {
        playAnim(activity, it)
        val start = tvInput.selectionStart
        val end = tvInput.selectionEnd
        if (start > 0 || start != end) {
            if (start == end) {
                tvInput.text.delete(start - 1, start)
            } else {
                tvInput.text.delete(start, end)
            }
            updatePreview(tvInput, tvResult)
            saveInput(tvInput.text.toString())
        }
    }

    // Tombol Klik Hasil (Untuk Apply Sugest/Koreksi)
    tvResult.setOnClickListener {
        val currentRes = tvResult.text.toString()
        if (currentRes.contains("?")) {
            val suggestion = currentRes.split("\"").getOrNull(1)
            if (suggestion != null) {
                tvInput.setText(suggestion)
                tvInput.setSelection(tvInput.text.length)
                updatePreview(tvInput, tvResult)
                saveInput(suggestion)
            }
        }
    }

    // Tombol Tahan Hasil (Untuk Lihat Cara Hitung / Explanation)
    tvResult.setOnLongClickListener {
        val input = tvInput.text.toString()
        if (input.isNotEmpty() && input.any { it.isDigit() }) {
            showCalculationSteps(activity, input)
        }
        true
    }

    // Tombol Mode Switcher (Scientific Menu)
    val scientificGrid = view.findViewById<GridLayout>(R.id.scientificGridLayout)
    view.findViewById<Button>(R.id.btn_mode_switcher).setOnClickListener {
        playAnim(activity, it)
        val rootLayout = view as ViewGroup
        TransitionManager.beginDelayedTransition(rootLayout, Fade().setDuration(200))

        if (scientificGrid.visibility == View.GONE) {
            scientificGrid.visibility = View.VISIBLE
            it.animate().rotation(180f).setDuration(300).start()
        } else {
            scientificGrid.visibility = View.GONE
            it.animate().rotation(0f).setDuration(300).start()
        }
    }

    // Daftarkan Tombol Scientific
    setupScientificLogic(activity, view, tvInput, tvResult, insertText)

    // Tombol Operator & Hitung
    setupOperatorLogic(activity, view, tvInput, tvResult, insertText)
    
    // Tombol Angka (0-9, 00, Koma)
    setupNumericLogic(activity, view, tvInput, tvResult, insertText)
}

private fun setupNumericLogic(activity: AppCompatActivity, view: View, tvInput: EditText, tvResult: TextView, insertText: (String) -> Unit) {
    val numericButtons = listOf(
        R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
        R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
        R.id.btn_nolnol, R.id.btn_koma
    )

    numericButtons.forEach { id ->
        view.findViewById<Button>(id).setOnClickListener { btn ->
            playAnim(activity, btn)
            val text = (btn as Button).text.toString()
            
            if (id == R.id.btn_koma) {
                val currentInput = tvInput.text.toString()
                val cursorIdx = tvInput.selectionStart
                val beforeCursor = currentInput.substring(0, cursorIdx)
                val lastNumber = beforeCursor.split(Regex("[+\\-*/%^()]")).last()
                
                if (!lastNumber.contains(",")) {
                    if (lastNumber.isEmpty()) insertText("0,") else insertText(",")
                }
            } else if (id == R.id.btn_nolnol) {
                insertText("00")
            } else {
                insertText(text)
            }
        }
    }
}

private fun setupOperatorLogic(activity: AppCompatActivity, view: View, tvInput: EditText, tvResult: TextView, insertText: (String) -> Unit) {
    val operators = mapOf(
        R.id.btn_tambah to "+", R.id.btn_kurang to "-",
        R.id.btn_kali to "*", R.id.btn_bagi to "/", R.id.btn_persen to "%"
    )

    operators.forEach { (id, symbol) ->
        view.findViewById<Button>(id).setOnClickListener {
            playAnim(activity, it)
            insertText(symbol)
        }
    }

    view.findViewById<Button>(R.id.btn_equal).setOnClickListener {
        playAnim(activity, it)
        val input = tvInput.text.toString()
        if (input.isNotEmpty()) {
            val res = calculate(input)
            if (res != "Error") {
                tvInput.setText(res)
                tvInput.setSelection(tvInput.text.length)
                tvResult.text = "0"
                tvResult.alpha = 0.5f
                val pref = activity.getSharedPreferences("kalkulator_prefs", Context.MODE_PRIVATE)
                pref.edit().putString("last_input", res).apply()
            }
        }
    }
}

private fun setupScientificLogic(activity: AppCompatActivity, view: View, tvInput: EditText, tvResult: TextView, insertText: (String) -> Unit) {
    val scientificButtons = mapOf(
        R.id.btn_kurung_buka to "(", R.id.btn_kurung_tutup to ")",
        R.id.btn_akar to "sqrt(", R.id.btn_pangkat to "^",
        R.id.btn_sin to "sin(", R.id.btn_cos to "cos(", R.id.btn_tan to "tan(",
        R.id.btn_log to "log10(", R.id.btn_ln to "log(", R.id.btn_pi to "π", R.id.btn_e to "e"
    )

    scientificButtons.forEach { (id, symbol) ->
        view.findViewById<Button>(id).setOnClickListener {
            playAnim(activity, it)
            insertText(symbol)
        }
    }

    view.findViewById<Button>(R.id.btn_inv).setOnClickListener {
        playAnim(activity, it)
        val currentText = tvInput.text.toString()
        if (currentText.isNotEmpty()) {
            if (currentText.startsWith("-")) {
                tvInput.setText(currentText.substring(1))
            } else {
                tvInput.setText("-$currentText")
            }
            tvInput.setSelection(tvInput.text.length)
            updatePreview(tvInput, tvResult)
        }
    }
}

private fun updatePreview(tvInput: EditText, tvResult: TextView) {
    val input = tvInput.text.toString()
    if (input.startsWith("%")) {
        val numberPart = input.substring(1)
        if (numberPart.isNotEmpty() && numberPart.all { it.isDigit() || it == ',' }) {
            tvResult.text = "Maksud anda \"$numberPart%\"?"
            tvResult.alpha = 1.0f
            tvResult.setTextColor(android.graphics.Color.YELLOW)
            return
        }
    }
    tvResult.setTextColor(android.graphics.Color.WHITE)
    if (input.isEmpty() || !input.any { it.isDigit() }) {
        tvResult.text = "0"
        tvResult.alpha = 0.5f
        return
    }
    if (input.last() in "+-*/%^(") {
        tvResult.alpha = 0.5f
        return
    }
    val res = calculate(input)
    if (res != "Error") {
        tvResult.text = res
        tvResult.alpha = 0.5f
    }
}

private fun calculate(expression: String): String {
    return try {
        var cleaned = expression.replace(",", ".").replace("%", "/100").replace("π", "PI").replace("e", "E")
        val openBrackets = cleaned.count { it == '(' }
        val closeBrackets = cleaned.count { it == ')' }
        repeat(openBrackets - closeBrackets) { cleaned += ")" }
        if (cleaned.isNotEmpty() && cleaned.last() in "+-*/") cleaned = cleaned.dropLast(1)
        val result = ExpressionBuilder(cleaned).build().evaluate()
        if (result.isNaN() || result.isInfinite()) return "Error"
        val longResult = result.toLong()
        if (result == longResult.toDouble()) longResult.toString() else "%.8f".format(java.util.Locale.US, result).trimEnd('0').trimEnd('.')
    } catch (e: Exception) { "Error" }
}

private fun playAnim(activity: AppCompatActivity, view: View) {
    val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
    view.startAnimation(anim)
}
