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
        // Konstan Pi dengan 40 digit presisi sesuai permintaan
        val PI_VAL = "3.1415926535897932384626433832795028841971"
        
        // 1. Normalisasi awal: Ubah koma ke titik untuk perhitungan
        var input = expression.replace(",", ".")

        // 2. High Precision Pi Handler (40 Digits)
        // Menangani π saja, π[angka], atau [angka]π dengan BigDecimal agar presisi tetap 40 angka
        val piMultiplyRegex = Regex("^π(\\d+\\.?\\d*)$")
        val numberPiRegex = Regex("^(\\d+\\.?\\d*)π$")
        
        if (input == "π") return PI_VAL
        
        piMultiplyRegex.find(input)?.let {
            val num = it.groupValues[1]
            return BigDecimal(PI_VAL).multiply(BigDecimal(num)).stripTrailingZeros().toPlainString()
        }
        numberPiRegex.find(input)?.let {
            val num = it.groupValues[1]
            return BigDecimal(PI_VAL).multiply(BigDecimal(num)).stripTrailingZeros().toPlainString()
        }

        // 3. Persiapan untuk exp4j (Pembersihan simbol)
        var cleaned = input.replace("π", "(PI)").replace("e", "(E)")
            
        // 4. Advanced Percentage Handler
        // Sekarang mendukung angka desimal (misal 5.5%) dan bisa berlapis (5%*5%)
        cleaned = cleaned.replace(Regex("(\\d+\\.?\\d*)%"), "($1/100)")

        // 5. Implicit Multiplication (Perkalian Otomatis)
        cleaned = cleaned.replace(Regex("(\\d+)(\\()"), "$1*$2") // 3(2) -> 3*(2)
        cleaned = cleaned.replace(Regex("(\\))(\\d+)"), "$1*$2") // (2)3 -> (2)*3
        cleaned = cleaned.replace(Regex("(\\d+)(PI|E)"), "$1*$2") // 3π -> 3*PI
        cleaned = cleaned.replace(Regex("(PI|E)(\\d+)"), "$1*$2") // π3 -> PI*3
        cleaned = cleaned.replace(Regex("(\\))(\\()"), "$1*$2")  // (2)(3) -> (2)*(3)

        // Tutup kurung otomatis jika user lupa
        val openBrackets = cleaned.count { it == '(' }
        val closeBrackets = cleaned.count { it == ')' }
        repeat(openBrackets - closeBrackets) { cleaned += ")" }
        
        // Hapus operator gantung di akhir
        if (cleaned.isNotEmpty() && cleaned.last() in "+-*/^") cleaned = cleaned.dropLast(1)
        
        // 6. Evaluasi menggunakan exp4j
        val result = ExpressionBuilder(cleaned).build().evaluate()
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

private fun showCalculationSteps(activity: AppCompatActivity, input: String) {
    // Implementasi BottomSheet untuk penjelasan langkah hitung (TODO)
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

