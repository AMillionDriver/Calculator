package com.axoloth.calculator.by.sky.logic

import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.axoloth.calculator.by.sky.MainActivity
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.screen.renderSettingsScreen
import com.axoloth.calculator.by.sky.screen.renderWeightScreen
import com.google.android.material.snackbar.Snackbar
import net.objecthunter.exp4j.ExpressionBuilder

/**
 * Mengatur semua logika tombol untuk KalkulatorScreen.
 */
fun setupKalkulatorLogic(activity: AppCompatActivity, view: View, tvInput: TextView, tvResult: TextView) {

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
        tvInput.text = ""
        tvResult.text = "0"
        tvResult.alpha = 1.0f
    }

    // Tombol Backspace
    view.findViewById<Button>(R.id.btn_backspace).setOnClickListener {
        playAnim(activity, it)
        val currentText = tvInput.text.toString()
        if (currentText.isNotEmpty()) {
            tvInput.text = currentText.dropLast(1)
            updatePreview(tvInput, tvResult)
        }
    }

    // Tombol Klik Hasil (Untuk Apply Sugest/Koreksi)
    tvResult.setOnClickListener {
        val currentRes = tvResult.text.toString()
        if (currentRes.contains("?")) { // Jika itu adalah saran
            val suggestion = currentRes.split("\"").getOrNull(1) // Ambil teks di dalam tanda kutip
            if (suggestion != null) {
                tvInput.text = suggestion
                updatePreview(tvInput, tvResult)
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
    setupScientificLogic(activity, view, tvInput, tvResult)

    // Tombol Operator & Hitung
    setupOperatorLogic(activity, view, tvInput, tvResult)
    
    // Tombol Angka (0-9, 00, Koma)
    setupNumericLogic(activity, view, tvInput, tvResult)
}

private fun setupNumericLogic(activity: AppCompatActivity, view: View, tvInput: TextView, tvResult: TextView) {
    val numericButtons = listOf(
        R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
        R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
        R.id.btn_nolnol, R.id.btn_koma
    )

    numericButtons.forEach { id ->
        view.findViewById<Button>(id).setOnClickListener { btn ->
            playAnim(activity, btn)
            val text = (btn as Button).text.toString()
            val currentInput = tvInput.text.toString()

            if (id == R.id.btn_koma) {
                // Cari angka terakhir yang sedang diketik (setelah operator terakhir)
                val lastNumber = currentInput.split(Regex("[+\\-*/%]")).last()
                // Jika angka terakhir belum ada koma, baru boleh tambah koma
                if (!lastNumber.contains(",")) {
                    if (lastNumber.isEmpty()) tvInput.append("0")
                    tvInput.append(",")
                }
            } else if (id == R.id.btn_nolnol) {
                // Cegah input "00" di awal
                if (currentInput.isNotEmpty() && currentInput != "0") {
                    tvInput.append("00")
                }
            } else {
                // Jika input sekarang cuma "0", ganti dengan angka baru (kecuali koma)
                if (currentInput == "0") {
                    tvInput.text = text
                } else {
                    tvInput.append(text)
                }
            }
            updatePreview(tvInput, tvResult)
        }
    }
}

private fun setupOperatorLogic(activity: AppCompatActivity, view: View, tvInput: TextView, tvResult: TextView) {
    val operators = mapOf(
        R.id.btn_tambah to "+",
        R.id.btn_kurang to "-",
        R.id.btn_kali to "*",
        R.id.btn_bagi to "/",
        R.id.btn_persen to "%"
    )

    operators.forEach { (id, symbol) ->
        view.findViewById<Button>(id).setOnClickListener {
            playAnim(activity, it)
            val currentInput = tvInput.text.toString()
            
            if (currentInput.isNotEmpty()) {
                val lastChar = currentInput.last()
                // Jika karakter terakhir adalah operator, ganti dengan yang baru
                if (lastChar in "+-*/%") {
                    tvInput.text = currentInput.dropLast(1) + symbol
                } else {
                    tvInput.append(symbol)
                }
            }
            updatePreview(tvInput, tvResult)
        }
    }

    // Tombol Sama Dengan (=)
    view.findViewById<Button>(R.id.btn_equal).setOnClickListener {
        playAnim(activity, it)
        val input = tvInput.text.toString()
        if (input.isNotEmpty()) {
            val res = calculate(input)
            if (res != "Error") {
                tvInput.text = res
                tvResult.text = "0"
                tvResult.alpha = 0.5f
            }
        }
    }
}

private fun setupScientificLogic(activity: AppCompatActivity, view: View, tvInput: TextView, tvResult: TextView) {
    val scientificButtons = mapOf(
        R.id.btn_kurung_buka to "(",
        R.id.btn_kurung_tutup to ")",
        R.id.btn_akar to "sqrt(",
        R.id.btn_pangkat to "^",
        R.id.btn_sin to "sin(",
        R.id.btn_cos to "cos(",
        R.id.btn_tan to "tan(",
        R.id.btn_log to "log10(",
        R.id.btn_ln to "log(",
        R.id.btn_pi to "π",
        R.id.btn_e to "e"
    )

    scientificButtons.forEach { (id, symbol) ->
        view.findViewById<Button>(id).setOnClickListener {
            playAnim(activity, it)
            tvInput.append(symbol)
            updatePreview(tvInput, tvResult)
        }
    }

    // Tombol Invert (±)
    view.findViewById<Button>(R.id.btn_inv).setOnClickListener {
        playAnim(activity, it)
        val currentText = tvInput.text.toString()
        if (currentText.isNotEmpty()) {
            if (currentText.startsWith("-")) {
                tvInput.text = currentText.substring(1)
            } else {
                tvInput.text = "-$currentText"
            }
            updatePreview(tvInput, tvResult)
        }
    }
}

private fun updatePreview(tvInput: TextView, tvResult: TextView) {
    val input = tvInput.text.toString()
    
    // 1. Cek Kesalahan Input Populer (Contoh: % di depan angka)
    if (input.startsWith("%")) {
        val numberPart = input.substring(1)
        if (numberPart.isNotEmpty() && numberPart.all { it.isDigit() || it == ',' }) {
            tvResult.text = "Maksud anda \"$numberPart%\"?"
            tvResult.alpha = 1.0f
            tvResult.setTextColor(android.graphics.Color.YELLOW)
            return
        }
    }
    
    // Reset warna jika normal
    tvResult.setTextColor(android.graphics.Color.WHITE)

    // 2. Cek apakah input layak di-preview (minimal ada operator atau angka yang lengkap)
    if (input.isEmpty() || !input.any { it.isDigit() }) {
        tvResult.text = "0"
        tvResult.alpha = 0.5f
        return
    }

    // Jangan preview jika input berakhir dengan operator atau kurung buka
    if (input.last() in "+-*/%^(") {
        tvResult.alpha = 0.5f
        return
    }

    val res = calculate(input)
    if (res != "Error") {
        tvResult.text = res
        tvResult.alpha = 0.5f
    } else {
        // Tetap biarkan hasil sebelumnya atau kosongkan jika error parah
    }
}

private fun calculate(expression: String): String {
    return try {
        // 1. Bersihkan expression agar dipahami oleh ExpressionBuilder
        var cleaned = expression
            .replace(",", ".")       // Ganti koma ke titik (decimal)
            .replace("%", "/100")    // Persen
            .replace("π", "PI")      // Konstanta PI
            .replace("e", "E")       // Konstanta E
            .replace("sqrt(", "sqrt(") // Sqrt tetap (sudah benar dari append)
            .replace("^", "^")         // Pangkat
        
        // 2. Jika ada kurung yang belum ditutup, tutup secara otomatis
        val openBrackets = cleaned.count { it == '(' }
        val closeBrackets = cleaned.count { it == ')' }
        repeat(openBrackets - closeBrackets) {
            cleaned += ")"
        }

        // 3. Jika diakhiri operator dasar, hapus operator terakhirnya dulu
        if (cleaned.isNotEmpty() && cleaned.last() in "+-*/") {
            cleaned = cleaned.dropLast(1)
        }

        val result = ExpressionBuilder(cleaned).build().evaluate()
        
        // 4. Format Hasil
        if (result.isNaN() || result.isInfinite()) return "Error"
        
        val longResult = result.toLong()
        if (result == longResult.toDouble()) {
            longResult.toString()
        } else {
            "%.8f".format(java.util.Locale.US, result).trimEnd('0').trimEnd('.')
        }
    } catch (e: Exception) {
        "Error"
    }
}

private fun playAnim(activity: AppCompatActivity, view: View) {
    val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
    view.startAnimation(anim)
}
