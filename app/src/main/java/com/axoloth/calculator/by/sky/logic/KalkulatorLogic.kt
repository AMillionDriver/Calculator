package com.axoloth.calculator.by.sky.logic

import android.content.Context
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.Fragment
import com.axoloth.calculator.by.sky.MainActivity
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.database.AppDatabase
import com.axoloth.calculator.by.sky.database.HistoryEntity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.objecthunter.exp4j.ExpressionBuilder

/**
 * Mengatur semua logika tombol untuk KalkulatorScreen dengan Cursor Editing.
 */
fun setupKalkulatorLogic(activity: AppCompatActivity, view: View, tvInput: EditText, tvResult: TextView) {
    val pref = activity.getSharedPreferences("kalkulator_prefs", Context.MODE_PRIVATE)
    val nestedScroll = view.findViewById<NestedScrollView>(R.id.nestedScroll)

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

    // Helper untuk Cursor Input dengan Fix Scroll Jump
    val insertText: (String) -> Unit = { text ->
        val scrollY = nestedScroll?.scrollY ?: 0
        
        val start = tvInput.selectionStart
        val end = tvInput.selectionEnd
        tvInput.text.replace(start, end, text)
        updatePreview(tvInput, tvResult)
        saveInput(tvInput.text.toString())

        // Paksa tetap di posisi scroll lama agar tidak loncat saat Split Screen
        nestedScroll?.post {
            nestedScroll.scrollTo(0, scrollY)
        }
    }

    // Tombol Settings
    view.findViewById<Button>(R.id.btn_settings).setOnClickListener {
        playAnim(activity, it)
        navigateToFragment(activity, com.axoloth.calculator.by.sky.ui.fragments.SettingsFragment())
    }

    // Tombol Kurs
    view.findViewById<Button>(R.id.btn_kurs).setOnClickListener {
        playAnim(activity, it)
        navigateToFragment(activity, com.axoloth.calculator.by.sky.ui.fragments.KursFragment())
    }

    // Tombol Weight
    view.findViewById<Button>(R.id.btn_weight).setOnClickListener {
        playAnim(activity, it)
        navigateToFragment(activity, com.axoloth.calculator.by.sky.ui.fragments.WeightFragment())
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
    view.findViewById<Button>(R.id.btn_backspace).apply {
        setOnClickListener {
            playAnim(activity, it)
            val start = tvInput.selectionStart
            val end = tvInput.selectionEnd
            if (start > 0 || start != end) {
                if (start == end) {
                    tvInput.text.delete(start - 1, start)
                } else {
                    tvInput.text.delete(start, end)
                }
                formatInputThousand(tvInput)
                updatePreview(tvInput, tvResult)
                saveInput(tvInput.text.toString())
            }
        }
        setOnLongClickListener {
            playAnim(activity, it)
            tvInput.setText("")
            tvResult.text = "0"
            tvResult.alpha = 0.5f
            saveInput("")
            true
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

    // Tombol Mode Switcher (Scientific Menu)
    val scientificGrid = view.findViewById<ConstraintLayout>(R.id.scientificGridLayout)
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

    // Tombol History
    view.findViewById<ImageView>(R.id.btn_history).setOnClickListener {
        playAnim(activity, it)
        showHistoryBottomSheet(activity, tvInput, tvResult)
    }

    // Daftarkan Tombol Scientific
    setupScientificLogic(activity, view, tvInput, tvResult, insertText)

    // Klik lama pada Mode Switcher untuk menu Advanced Math
    view.findViewById<Button>(R.id.btn_mode_switcher).setOnLongClickListener {
        playAnim(activity, it)
        showAdvancedMathMenu(activity, insertText)
        true
    }

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
                formatInputThousand(tvInput)
            } else {
                insertText(text)
                formatInputThousand(tvInput)
            }
        }
    }
}

private fun formatInputThousand(tvInput: EditText) {
    val originalText = tvInput.text.toString()
    if (originalText.isEmpty()) return
    
    val cursorPosition = tvInput.selectionStart
    val cleanText = originalText.replace(".", "")
    
    val formatted = StringBuilder()
    
    // Regex untuk memisahkan angka dan non-angka
    val parts = cleanText.split(Regex("(?=[+\\-*/%^()])|(?<=[+\\-*/%^()])"))
    
    for (part in parts) {
        if (part.isEmpty()) continue
        if (part.any { it.isDigit() }) {
            val numParts = part.split(",")
            val integerPart = numParts[0]
            val decimalPart = if (numParts.size > 1) "," + numParts[1] else ""
            
            // Format ribuan dengan titik
            val formattedInt = if (integerPart.isNotEmpty()) {
                integerPart.reversed().chunked(3).joinToString(".").reversed()
            } else ""
            formatted.append(formattedInt).append(decimalPart)
        } else {
            formatted.append(part)
        }
    }
    
    if (formatted.toString() != originalText) {
        tvInput.setText(formatted.toString())
        // Sederhanakan kursor: taruh di akhir saja untuk menghindari bug loncat yang rumit
        // Idealnya dihitung beda titiknya, tapi untuk stabilitas kita taruh di akhir
        tvInput.setSelection(tvInput.text.length)
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

    val btnEqual = view.findViewById<Button>(R.id.btn_equal)
    btnEqual.setOnClickListener {
        playAnim(activity, it)
        val input = tvInput.text.toString()
        if (input.isNotEmpty()) {
            val res = calculate(input)
            if (res != "Error") {
                // Simpan ke Riwayat Database
                saveToHistory(activity, input, res)
                
                tvInput.setText(res)
                tvInput.setSelection(tvInput.text.length)
                tvResult.text = "0"
                tvResult.alpha = 0.5f
                val pref = activity.getSharedPreferences("kalkulator_prefs", Context.MODE_PRIVATE)
                pref.edit().putString("last_input", res).apply()
            }
        }
    }

    // Long click pada "=" untuk memunculkan penjelasan cara hitung
    btnEqual.setOnLongClickListener {
        val input = tvInput.text.toString()
        if (input.isNotEmpty()) {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            showCalculationSteps(activity, input)
        }
        true
    }
}

private fun showAdvancedMathMenu(activity: AppCompatActivity, insertText: (String) -> Unit) {
    val dialog = BottomSheetDialog(activity)
    val view = activity.layoutInflater.inflate(R.layout.layout_advanced_math, null)
    dialog.setContentView(view)

    val buttons = mapOf(
        R.id.btn_gcd to "gcd(",
        R.id.btn_lcm to "lcm(",
        R.id.btn_max to "max(",
        R.id.btn_min to "min(",
        R.id.btn_logb to "logb(",
        R.id.btn_mod to "mod("
    )

    buttons.forEach { (id, formula) ->
        view.findViewById<Button>(id).setOnClickListener {
            insertText(formula)
            dialog.dismiss()
        }
    }

    dialog.show()
}

private fun saveToHistory(activity: AppCompatActivity, expression: String, result: String) {
    activity.lifecycleScope.launch(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(activity)
        val historyEntry = HistoryEntity(expression = expression, result = result)
        database.historyDao().insertHistory(historyEntry)
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

    // Tombol INV diubah jadi Faktorial (!) karena lebih berguna
    view.findViewById<Button>(R.id.btn_inv).apply {
        text = "!"
        setOnClickListener {
            playAnim(activity, it)
            insertText("!")
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
    // Izinkan persen di akhir untuk preview
    if (input.last() in "+-*/^(") {
        tvResult.alpha = 0.5f
        return
    }
    val res = calculate(input)
    if (res != "Error") {
        tvResult.text = formatDisplayResult(res)
        tvResult.alpha = 0.5f
    }
}

private fun formatDisplayResult(value: String): String {
    if (value == "Error") return value
    return try {
        val isNegative = value.startsWith("-")
        val absValue = if (isNegative) value.substring(1) else value
        
        val parts = absValue.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else null

        // Format ribuan dengan titik
        val formattedInt = integerPart.reversed().chunked(3).joinToString(".").reversed()
        
        // Desimal dengan koma
        val result = if (decimalPart != null) "$formattedInt,$decimalPart" else formattedInt
        if (isNegative) "-$result" else result
    } catch (e: Exception) {
        value
    }
}

private fun calculate(expression: String): String {
    return try {
        // Konstan Pi dengan 40 digit presisi
        val PI_VAL = "3.1415926535897932384626433832795028841971"
        
        // 1. Normalisasi: Hapus titik ribuan dan ubah koma desimal ke titik standar
        var input = expression.replace(".", "").replace(",", ".")

        // 2. High Precision Pi Handler
        if (input == "π") return PI_VAL
        val piMultiplyRegex = Regex("^π(\\d+\\.?\\d*)$")
        val numberPiRegex = Regex("^(\\d+\\.?\\d*)π$")
        
        piMultiplyRegex.find(input)?.let {
            val num = it.groupValues[1]
            return BigDecimal(PI_VAL).multiply(BigDecimal(num)).stripTrailingZeros().toPlainString()
        }
        numberPiRegex.find(input)?.let {
            val num = it.groupValues[1]
            return BigDecimal(PI_VAL).multiply(BigDecimal(num)).stripTrailingZeros().toPlainString()
        }

        // 3. Persiapan untuk exp4j
        var cleaned = input.replace("π", "(PI)").replace("e", "(E)")
            
        // 4. Advanced Percentage Handler (Sangat Penting!)
        // Handle: "100 + 10%" -> "100 + (100 * 10 / 100)"
        cleaned = cleaned.replace(Regex("(\\d+\\.?\\d*)\\s*([+\\-])\\s*(\\d+\\.?\\d*)\\s*%")) {
            val base = it.groupValues[1]
            val op = it.groupValues[2]
            val percent = it.groupValues[3]
            "$base$op($base*$percent/100)"
        }
        // Handle: "5 * 5%" -> "5 * (5/100)"
        cleaned = cleaned.replace(Regex("(\\d+\\.?\\d*)\\s*%"), "($1/100)")

        // 5. Implicit Multiplication (Perkalian Otomatis)
        cleaned = cleaned.replace(Regex("(\\d+)(\\()"), "$1*$2") // 3(2) -> 3*(2)
        cleaned = cleaned.replace(Regex("(\\))(\\d+)"), "$1*$2") // (2)3 -> (2)*3
        cleaned = cleaned.replace(Regex("(\\d+)(PI|E|fact|gcd|lcm|max|min|logb)"), "$1*$2") // 3π -> 3*PI
        cleaned = cleaned.replace(Regex("(PI|E)(\\d+)"), "$1*$2") // π3 -> PI*3
        cleaned = cleaned.replace(Regex("(\\))(\\()"), "$1*$2")  // (2)(3) -> (2)*(3)

        // Tutup kurung otomatis jika user lupa
        val openBrackets = cleaned.count { it == '(' }
        val closeBrackets = cleaned.count { it == ')' }
        repeat(openBrackets - closeBrackets) { cleaned += ")" }
        
        // Hapus operator gantung di akhir
        if (cleaned.isNotEmpty() && cleaned.last() in "+-*/^") cleaned = cleaned.dropLast(1)
        
        // 6. Custom Functions Library
        val customFunctions = mutableListOf<net.objecthunter.exp4j.function.Function>()

        // 6a. Faktorial (fact)
        customFunctions.add(object : net.objecthunter.exp4j.function.Function("fact", 1) {
            override fun apply(vararg args: Double): Double {
                val arg = args[0]
                if (arg < 0 || arg > 170) return Double.NaN
                if (arg % 1 != 0.0) return gamma(arg + 1)
                var res = 1.0
                for (i in 1..arg.toInt()) res *= i
                return res
            }
            private fun gamma(x: Double): Double {
                val p = doubleArrayOf(0.99999999999980993, 676.5203681218851, -1259.1392167224028, 771.32342877765313, -176.61502916214059, 12.507343278686905, -0.13857109526572012, 9.9843695780195716e-6, 1.5056327351493116e-7)
                var g = 7.0; var y = x
                if (y < 0.5) return Math.PI / (Math.sin(Math.PI * y) * gamma(1.0 - y))
                y -= 1.0; var a = p[0]; val t = y + g + 0.5
                for (i in 1 until p.size) a += p[i] / (y + i)
                return Math.sqrt(2.0 * Math.PI) * Math.pow(t, y + 0.5) * Math.exp(-t) * a
            }
        })

        // 6b. GCD (FPB) & LCM (KPK)
        customFunctions.add(object : net.objecthunter.exp4j.function.Function("gcd", 2) {
            override fun apply(vararg args: Double): Double {
                var a = Math.abs(args[0].toLong()); var b = Math.abs(args[1].toLong())
                while (b > 0) { a %= b; val t = a; a = b; b = t }
                return a.toDouble()
            }
        })
        customFunctions.add(object : net.objecthunter.exp4j.function.Function("lcm", 2) {
            override fun apply(vararg args: Double): Double {
                if (args[0] == 0.0 || args[1] == 0.0) return 0.0
                val a = Math.abs(args[0]); val b = Math.abs(args[1])
                var x = a.toLong(); var y = b.toLong()
                while (y > 0) { x %= y; val t = x; x = y; y = t }
                return (a * b) / x
            }
        })

        // 6c. Log Basis Bebas (logb(angka, basis))
        customFunctions.add(object : net.objecthunter.exp4j.function.Function("logb", 2) {
            override fun apply(vararg args: Double): Double = Math.log(args[0]) / Math.log(args[1])
        })

        // 6d. Max & Min
        customFunctions.add(object : net.objecthunter.exp4j.function.Function("max", 2) {
            override fun apply(vararg args: Double): Double = Math.max(args[0], args[1])
        })
        customFunctions.add(object : net.objecthunter.exp4j.function.Function("min", 2) {
            override fun apply(vararg args: Double): Double = Math.min(args[0], args[1])
        })

        // 6e. Modulo (karena % sudah dipakai persen)
        customFunctions.add(object : net.objecthunter.exp4j.function.Function("mod", 2) {
            override fun apply(vararg args: Double): Double = args[0] % args[1]
        })

        // Ubah format 5! menjadi fact(5)
        while (cleaned.contains("!")) {
            val lastIdx = cleaned.indexOf("!")
            var i = lastIdx - 1; var bc = 0
            while (i >= 0) {
                if (cleaned[i] == ')') bc++ else if (cleaned[i] == '(') bc--
                if (bc == 0 && !cleaned[i].isDigit() && cleaned[i] != '.' && cleaned[i] != ')' && cleaned[i] != '(') break
                i--
            }
            val target = cleaned.substring(i + 1, lastIdx)
            cleaned = cleaned.replaceFirst("$target!", "fact($target)")
        }

        // 7. Evaluasi menggunakan exp4j dengan library kustom
        val result = ExpressionBuilder(cleaned)
            .functions(customFunctions)
            .build()
            .evaluate()
        if (result.isNaN() || result.isInfinite()) return "Error"
        
        val longResult = result.toLong()
        if (result == longResult.toDouble()) {
            longResult.toString()
        } else {
            // Gunakan 15 desimal untuk hasil umum agar tetap akurat dan bersih
            "%.15f".format(java.util.Locale.US, result).trimEnd('0').trimEnd('.')
        }
    } catch (e: Exception) { "Error" }
}

private fun navigateToFragment(activity: AppCompatActivity, fragment: Fragment) {
    activity.supportFragmentManager.beginTransaction()
        .setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        .replace(R.id.fragment_container, fragment)
        .addToBackStack(null)
        .commit()
}

private fun playAnim(activity: AppCompatActivity, view: View) {
    val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
    view.startAnimation(anim)
}

private fun showHistoryBottomSheet(activity: AppCompatActivity, tvInput: EditText, tvResult: TextView) {
    val dialog = BottomSheetDialog(activity)
    val view = activity.layoutInflater.inflate(R.layout.layout_history_bottom_sheet, null)
    dialog.setContentView(view)

    val rvHistory = view.findViewById<RecyclerView>(R.id.rv_history)
    val tvEmpty = view.findViewById<TextView>(R.id.tv_empty_history)
    val btnClear = view.findViewById<Button>(R.id.btn_clear_history)

    rvHistory.layoutManager = LinearLayoutManager(activity)

    activity.lifecycleScope.launch(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(activity)
        val historyList = database.historyDao().getAllHistory().toMutableList()

        withContext(Dispatchers.Main) {
            if (historyList.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvHistory.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvHistory.visibility = View.VISIBLE
                
                val adapter = HistoryAdapter(
                    items = historyList,
                    onItemClick = { history ->
                        tvInput.setText(history.expression) // Kembalikan ekspresinya
                        tvInput.setSelection(tvInput.text.length)
                        updatePreview(tvInput, tvResult)
                        dialog.dismiss()
                    },
                    onItemLongClick = { history ->
                        // Hapus satu item
                        android.app.AlertDialog.Builder(activity)
                            .setTitle("Hapus Riwayat?")
                            .setMessage("Apakah Anda ingin menghapus item ini?")
                            .setPositiveButton("Hapus") { _, _ ->
                                activity.lifecycleScope.launch(Dispatchers.IO) {
                                    database.historyDao().deleteById(history.id)
                                    withContext(Dispatchers.Main) {
                                        (rvHistory.adapter as? HistoryAdapter)?.removeItem(history)
                                        if (rvHistory.adapter?.itemCount == 0) {
                                            tvEmpty.visibility = View.VISIBLE
                                            rvHistory.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                    }
                )
                rvHistory.adapter = adapter
            }
        }
    }

    btnClear.setOnClickListener {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase(activity).historyDao().clearHistory()
            withContext(Dispatchers.Main) {
                dialog.dismiss()
            }
        }
    }

    dialog.show()
}

