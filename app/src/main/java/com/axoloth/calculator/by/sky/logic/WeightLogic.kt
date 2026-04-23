package com.axoloth.calculator.by.sky.logic

import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.axoloth.calculator.by.sky.MainActivity
import com.axoloth.calculator.by.sky.R
import java.util.Locale

/**
 * Logika Konversi Satuan dengan Cursor Editing dan Pemilihan Unit Custom.
 */
fun setupWeightLogic(activity: AppCompatActivity, view: View) {
    val btnBack: Button = view.findViewById(R.id.btnBack)
    val etValueFrom: EditText = view.findViewById(R.id.etValueFrom)
    val tvValueTo: TextView = view.findViewById(R.id.tvValueTo)
    val tvUnitFrom: TextView = view.findViewById(R.id.tvUnitFrom)
    val tvUnitTo: TextView = view.findViewById(R.id.tvUnitTo)
    val txtTitle: TextView = view.findViewById(R.id.txtConverterTitle)
    val adContainer: android.widget.FrameLayout = view.findViewById(R.id.native_ad_container)

    // Load Native Ads
    com.axoloth.calculator.by.sky.ads.NativeAdsLogic.loadAndShowNativeAd(activity, adContainer)

    // Matikan keyboard sistem tapi tetap aktifkan kursor
    etValueFrom.showSoftInputOnFocus = false
    etValueFrom.requestFocus()

    // State
    var currentCategory = "Berat"
    
    // Data Konversi Lengkap
    val unitsData = mapOf(
        "Berat" to listOf("Kilogram (kg)" to 1000.0, "Gram (g)" to 1.0, "Miligram (mg)" to 0.001, "Pound (lb)" to 453.592, "Ounce (oz)" to 28.3495),
        "Panjang" to listOf("Kilometer (km)" to 1000.0, "Meter (m)" to 1.0, "Centimeter (cm)" to 0.01, "Millimeter (mm)" to 0.001, "Inch (in)" to 0.0254, "Foot (ft)" to 0.3048),
        "Area" to listOf("Square Meter (m²)" to 1.0, "Square KM (km²)" to 1000000.0, "Hectare (ha)" to 10000.0, "Acre" to 4046.86),
        "Volume" to listOf("Liter (L)" to 1.0, "Milliliter (ml)" to 0.001, "Cubic Meter (m³)" to 1000.0, "Gallon" to 3.78541),
        "Suhu" to listOf("Celsius (°C)" to 1.0, "Fahrenheit (°F)" to 0.0, "Kelvin (K)" to 0.0),
        "Speed" to listOf("m/s" to 1.0, "km/h" to 0.277778, "mph" to 0.44704, "Knot" to 0.514444),
        "Pressure" to listOf("Pascal (Pa)" to 1.0, "Bar" to 100000.0, "PSI" to 6894.76, "Atmosphere (atm)" to 101325.0),
        "Power" to listOf("Watt (W)" to 1.0, "Kilowatt (kW)" to 1000.0, "Horsepower (hp)" to 745.7)
    )

    var fromUnit = unitsData["Berat"]!![0]
    var toUnit = unitsData["Berat"]!![1]

    fun calculateConversion() {
        val inputStr = etValueFrom.text.toString().replace(".", "").replace(",", ".")
        if (inputStr.isEmpty()) {
            tvValueTo.text = "0"
            return
        }

        try {
            val inputNum = inputStr.toDouble()
            val result = if (currentCategory == "Suhu") {
                convertTemperature(inputNum, tvUnitFrom.text.toString(), tvUnitTo.text.toString())
            } else {
                inputNum * (fromUnit.second / toUnit.second)
            }
            tvValueTo.text = formatResult(result)
        } catch (e: Exception) {
            tvValueTo.text = "0"
        }
    }

    // --- Cursor-Based Input Handling ---
    fun insertText(text: String) {
        val start = etValueFrom.selectionStart
        val end = etValueFrom.selectionEnd
        etValueFrom.text.replace(start, end, text)
        formatInputThousand(etValueFrom)
        calculateConversion()
    }

    fun deleteText() {
        val start = etValueFrom.selectionStart
        val end = etValueFrom.selectionEnd
        if (start > 0 || start != end) {
            if (start == end) {
                etValueFrom.text.delete(start - 1, start)
            } else {
                etValueFrom.text.delete(start, end)
            }
            formatInputThousand(etValueFrom)
        }
        calculateConversion()
    }

    // --- Popup Menu for Units ---
    fun showUnitMenu(anchor: View, isFrom: Boolean) {
        val popup = PopupMenu(activity, anchor)
        val categoryUnits = unitsData[currentCategory] ?: return
        
        categoryUnits.forEachIndexed { index, unit ->
            popup.menu.add(0, index, index, unit.first)
        }

        popup.setOnMenuItemClickListener { item ->
            val selectedUnit = categoryUnits[item.itemId]
            if (isFrom) {
                fromUnit = selectedUnit
                tvUnitFrom.text = selectedUnit.first
            } else {
                toUnit = selectedUnit
                tvUnitTo.text = selectedUnit.first
            }
            calculateConversion()
            true
        }
        popup.show()
    }

    tvUnitFrom.setOnClickListener { showUnitMenu(it, true) }
    tvUnitTo.setOnClickListener { showUnitMenu(it, false) }

    // --- Keypad Bindings ---
    val numericButtons = mapOf(
        R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
        R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
        R.id.btn8 to "8", R.id.btn9 to "9"
    )

    numericButtons.forEach { (id, value) ->
        view.findViewById<Button>(id).setOnClickListener {
            playAnim(activity, it)
            insertText(value)
        }
    }

    view.findViewById<Button>(R.id.btnKoma).setOnClickListener {
        playAnim(activity, it)
        if (!etValueFrom.text.contains(",")) insertText(",")
    }

    view.findViewById<Button>(R.id.btnDel).apply {
        setOnClickListener {
            playAnim(activity, it)
            deleteText()
        }
        setOnLongClickListener {
            playAnim(activity, it)
            etValueFrom.setText("")
            calculateConversion()
            true
        }
    }

    // --- Category Switch ---
    val categories = mapOf(
        R.id.btnBerat to "Berat", R.id.btnPanjang to "Panjang",
        R.id.btnArea to "Area", R.id.btnVolume to "Volume", R.id.btnSuhu to "Suhu",
        R.id.btnSpeed to "Speed", R.id.btnPressure to "Pressure", R.id.btnPower to "Power"
    )

    categories.forEach { (id, name) ->
        view.findViewById<Button>(id)?.setOnClickListener {
            playAnim(activity, it)
            currentCategory = name
            txtTitle.text = name
            fromUnit = unitsData[name]!![0]
            toUnit = unitsData[name]!![1]
            tvUnitFrom.text = fromUnit.first
            tvUnitTo.text = toUnit.first
            etValueFrom.setText("1")
            etValueFrom.setSelection(etValueFrom.text.length)
            calculateConversion()
            
            categories.keys.forEach { btnId ->
                view.findViewById<Button>(btnId)?.setTextColor(if(btnId == id) 0xFFFFFFFF.toInt() else 0x66FFFFFF.toInt())
            }
        }
    }

    btnBack.setOnClickListener {
        playAnim(activity, it)
        com.axoloth.calculator.by.sky.ads.NativeAdsLogic.destroyAd()
        activity.onBackPressedDispatcher.onBackPressed()
    }

    etValueFrom.requestFocus()
    calculateConversion()
}

private fun convertTemperature(value: Double, from: String, to: String): Double {
    // Normalisasi ke Celsius
    val celsius = when {
        from.contains("Celsius") -> value
        from.contains("Fahrenheit") -> (value - 32) * 5/9
        from.contains("Kelvin") -> value - 273.15
        else -> value
    }
    // Konversi ke tujuan
    return when {
        to.contains("Celsius") -> celsius
        to.contains("Fahrenheit") -> (celsius * 9/5) + 32
        to.contains("Kelvin") -> celsius + 273.15
        else -> celsius
    }
}

private fun formatResult(value: Double): String {
    val df = java.text.DecimalFormat("#,###.####", java.text.DecimalFormatSymbols(Locale("id", "ID")))
    return df.format(value)
}

private fun formatInputThousand(etInput: EditText) {
    val originalText = etInput.text.toString()
    if (originalText.isEmpty()) return
    
    val cleanText = originalText.replace(".", "")
    val numParts = cleanText.split(",")
    val integerPart = numParts[0]
    val decimalPart = if (numParts.size > 1) "," + numParts[1] else ""
    
    val formattedInt = if (integerPart.isNotEmpty()) {
        integerPart.reversed().chunked(3).joinToString(".").reversed()
    } else ""
    
    val formatted = formattedInt + decimalPart
    
    if (formatted != originalText) {
        etInput.setText(formatted)
        etInput.setSelection(etInput.text.length)
    }
}

private fun playAnim(activity: AppCompatActivity, view: View) {
    val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
    view.startAnimation(anim)
}
