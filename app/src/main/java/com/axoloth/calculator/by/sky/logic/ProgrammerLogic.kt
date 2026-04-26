package com.axoloth.calculator.by.sky.logic

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.axoloth.calculator.by.sky.R
import java.util.*

enum class BaseMode { HEX, DEC, OCT, BIN }

fun setupProgrammerLogic(activity: AppCompatActivity, view: View) {
    var currentBase = BaseMode.DEC
    var inputString = "0"

    val tvMain = view.findViewById<TextView>(R.id.tvMainDisplay)
    val tvHex = view.findViewById<TextView>(R.id.tvHexVal)
    val tvDec = view.findViewById<TextView>(R.id.tvDecVal)
    val tvOct = view.findViewById<TextView>(R.id.tvOctVal)
    val tvBin = view.findViewById<TextView>(R.id.tvBinVal)

    val rows = mapOf(
        BaseMode.HEX to view.findViewById<LinearLayout>(R.id.rowHex),
        BaseMode.DEC to view.findViewById<LinearLayout>(R.id.rowDec),
        BaseMode.OCT to view.findViewById<LinearLayout>(R.id.rowOct),
        BaseMode.BIN to view.findViewById<LinearLayout>(R.id.rowBin)
    )

    val buttons = mutableMapOf<Int, Button>()
    val ids = listOf(
        R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
        R.id.btnA, R.id.btnB, R.id.btnC, R.id.btnD, R.id.btnE, R.id.btnF,
        R.id.btnClear, R.id.btnDel, R.id.btnEqual, R.id.btnKMap
    )
    ids.forEach { id -> buttons[id] = view.findViewById(id) }

    fun updateUI() {
        // Highlight active row
        rows.forEach { (mode, row) ->
            row?.setBackgroundColor(if (mode == currentBase) 0x3300E5FF.toInt() else 0x00000000)
        }

        // Enable/Disable buttons based on base
        val decEnabled = currentBase == BaseMode.DEC || currentBase == BaseMode.HEX
        val hexEnabled = currentBase == BaseMode.HEX
        val octEnabled = currentBase != BaseMode.BIN
        
        for (i in 0..9) {
            val btn = buttons[activity.resources.getIdentifier("btn$i", "id", activity.packageName)]
            btn?.isEnabled = when(currentBase) {
                BaseMode.BIN -> i <= 1
                BaseMode.OCT -> i <= 7
                BaseMode.DEC -> true
                BaseMode.HEX -> true
            }
            btn?.setTextColor(if (btn?.isEnabled == true) 0xFFFFFFFF.toInt() else 0x66FFFFFF.toInt())
        }

        listOf(R.id.btnA, R.id.btnB, R.id.btnC, R.id.btnD, R.id.btnE, R.id.btnF).forEach { id ->
            buttons[id]?.isEnabled = hexEnabled
            buttons[id]?.setTextColor(if (hexEnabled) 0xFFFFFFFF.toInt() else 0x66FFFFFF.toInt())
        }

        // Convert and Display
        try {
            val value = if (inputString.isEmpty() || inputString == "-") 0L else {
                when (currentBase) {
                    BaseMode.HEX -> inputString.toLong(16)
                    BaseMode.DEC -> inputString.toLong()
                    BaseMode.OCT -> inputString.toLong(8)
                    BaseMode.BIN -> inputString.toLong(2)
                }
            }

            tvMain.text = inputString
            tvHex.text = value.toString(16).uppercase()
            tvDec.text = value.toString(10)
            tvOct.text = value.toString(8)
            tvBin.text = value.toString(2)

        } catch (e: Exception) {
            tvMain.text = "Error"
        }
    }

    // Row Click Listeners
    rows.forEach { (mode, row) ->
        row?.setOnClickListener {
            // Convert current input to new base before switching
            try {
                val value = if (inputString.isEmpty() || inputString == "-") 0L else {
                    when (currentBase) {
                        BaseMode.HEX -> inputString.toLong(16)
                        BaseMode.DEC -> inputString.toLong()
                        BaseMode.OCT -> inputString.toLong(8)
                        BaseMode.BIN -> inputString.toLong(2)
                    }
                }
                currentBase = mode
                inputString = when (currentBase) {
                    BaseMode.HEX -> value.toString(16).uppercase()
                    BaseMode.DEC -> value.toString(10)
                    BaseMode.OCT -> value.toString(8)
                    BaseMode.BIN -> value.toString(2)
                }
                updateUI()
            } catch (e: Exception) {}
        }
    }

    // Number & Hex Buttons
    val allInputButtons = mapOf(
        R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
        R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
        R.id.btn8 to "8", R.id.btn9 to "9", R.id.btnA to "A", R.id.btnB to "B",
        R.id.btnC to "C", R.id.btnD to "D", R.id.btnE to "E", R.id.btnF to "F"
    )

    allInputButtons.forEach { (id, char) ->
        view.findViewById<Button>(id).setOnClickListener {
            if (inputString == "0") inputString = char else inputString += char
            updateUI()
        }
    }

    // Controls
    view.findViewById<Button>(R.id.btnClear).setOnClickListener {
        inputString = "0"
        updateUI()
    }

    view.findViewById<Button>(R.id.btnDel).setOnClickListener {
        if (inputString.length > 1) {
            inputString = inputString.dropLast(1)
        } else {
            inputString = "0"
        }
        updateUI()
    }

    view.findViewById<Button>(R.id.btnBack).setOnClickListener {
        activity.onBackPressedDispatcher.onBackPressed()
    }

    // K-Map Solver Trigger
    view.findViewById<Button>(R.id.btnKMap).setOnClickListener {
        showKMapSolver(activity)
    }

    updateUI()
}

private fun showKMapSolver(activity: AppCompatActivity) {
    val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(activity)
    val view = activity.layoutInflater.inflate(R.layout.layout_kmap_solver, null)
    dialog.setContentView(view)

    val grid = view.findViewById<android.widget.GridLayout>(R.id.kmapGrid)
    val tvResult = view.findViewById<TextView>(R.id.tvKMapResult)
    val btnReset = view.findViewById<Button>(R.id.btnResetKMap)
    val btnStep = view.findViewById<Button>(R.id.btnKMapStep)
    val etManual = view.findViewById<android.widget.EditText>(R.id.etManualInput)

    val cellValues = IntArray(16) { 0 } // 0: False, 1: True, 2: Don't Care (X)
    var currentSteps = "Belum ada kalkulasi."

    // Urutan Gray Code untuk K-Map (00, 01, 11, 10)
    // Map index grid (0-15) ke index minterm standar (0-15)
    val grayToMinterm = intArrayOf(
        0, 1, 3, 2,
        4, 5, 7, 6,
        12, 13, 15, 14,
        8, 9, 11, 10
    )

    var isUpdatingFromGrid = false

    fun updateGridVisuals() {
        for (i in 0 until 16) {
            val btn = grid.getChildAt(i) as Button
            when (cellValues[i]) {
                0 -> { btn.text = "0"; btn.setBackgroundColor(android.graphics.Color.DKGRAY) }
                1 -> { btn.text = "1"; btn.setBackgroundColor(0xFF00E5FF.toInt()) }
                2 -> { btn.text = "X"; btn.setBackgroundColor(android.graphics.Color.GRAY) }
            }
        }
    }

    fun calculateKMap(updateManualField: Boolean = false) {
        val minterms = mutableListOf<Int>()
        val dontCares = mutableListOf<Int>()

        for (i in 0 until 16) {
            val mintermIdx = grayToMinterm[i]
            when (cellValues[i]) {
                1 -> minterms.add(mintermIdx)
                2 -> dontCares.add(mintermIdx)
            }
        }
        
        val resultObj = KMapLogic.solveWithSteps(4, minterms, dontCares)
        val result = resultObj.expression
        currentSteps = resultObj.steps
        
        tvResult.text = "F = $result"
        
        if (updateManualField) {
            isUpdatingFromGrid = true
            etManual.setText(result)
            isUpdatingFromGrid = false
        }
    }

    fun parseManualInput(input: String) {
        if (isUpdatingFromGrid) return
        
        // Reset cell values first
        for (i in 0 until 16) cellValues[i] = 0
        
        if (input.isEmpty()) {
            updateGridVisuals()
            tvResult.text = "F = 0"
            return
        }
        
        val cleanInput = input.uppercase().replace(" ", "")
        
        // Handle Minterm format: m(0,1,2)
        if (cleanInput.contains("M(") || cleanInput.contains("MIN(") || cleanInput.contains("M[")) {
            val numbers = Regex("\\d+").findAll(cleanInput).map { it.value.toInt() }.toList()
            for (num in numbers) {
                if (num in 0..15) {
                    val gridIdx = grayToMinterm.indexOf(num)
                    if (gridIdx != -1) cellValues[gridIdx] = 1
                }
            }
        } else {
            // Handle SOP format: AB + C'D
            val terms = cleanInput.split("+").map { it.trim() }
            for (term in terms) {
                if (term.isEmpty()) continue
                val mask = IntArray(4) { -1 } // A, B, C, D
                
                var i = 0
                while (i < term.length) {
                    val char = term[i]
                    val varIdx = when(char) { 'A' -> 0; 'B' -> 1; 'C' -> 2; 'D' -> 3; else -> -1 }
                    if (varIdx != -1) {
                        val isPrime = if (i + 1 < term.length && term[i + 1] == '\'') {
                            i++; 0
                        } else 1
                        mask[varIdx] = isPrime
                    }
                    i++
                }
                
                for (m in 0 until 16) {
                    var match = true
                    for (b in 0 until 4) {
                        val bitValue = (m shr (3 - b)) and 1
                        if (mask[b] != -1 && mask[b] != bitValue) { match = false; break }
                    }
                    if (match) cellValues[grayToMinterm.indexOf(m)] = 1
                }
            }
        }
        
        updateGridVisuals()
        calculateKMap(updateManualField = false)
    }

    etManual.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            parseManualInput(s.toString())
        }
        override fun afterTextChanged(s: android.text.Editable?) {}
    })

    // Inisialisasi 16 Sel
    for (i in 0 until 16) {
        val button = Button(activity).apply {
            layoutParams = android.widget.GridLayout.LayoutParams().apply {
                width = 120
                height = 120
                setMargins(4, 4, 4, 4)
            }
            text = "0"
            setBackgroundColor(android.graphics.Color.DKGRAY)
            setTextColor(android.graphics.Color.WHITE)
            textSize = 14f
            
            setOnClickListener {
                cellValues[i] = (cellValues[i] + 1) % 3
                when (cellValues[i]) {
                    0 -> { text = "0"; setBackgroundColor(android.graphics.Color.DKGRAY) }
                    1 -> { text = "1"; setBackgroundColor(0xFF00E5FF.toInt()) }
                    2 -> { text = "X"; setBackgroundColor(android.graphics.Color.GRAY) }
                }
                calculateKMap(updateManualField = true)
            }
        }
        grid.addView(button)
    }

    btnReset.setOnClickListener {
        for (i in 0 until 16) {
            cellValues[i] = 0
            val btn = grid.getChildAt(i) as Button
            btn.text = "0"
            btn.setBackgroundColor(android.graphics.Color.DKGRAY)
        }
        calculateKMap()
    }

    btnStep.setOnClickListener {
        val scroll = android.widget.ScrollView(activity)
        val tvSteps = TextView(activity).apply {
            text = currentSteps
            setPadding(40, 40, 40, 40)
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
            setTextColor(android.graphics.Color.WHITE)
        }
        scroll.addView(tvSteps)
        
        androidx.appcompat.app.AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog)
            .setTitle("Langkah Penyelesaian SOP")
            .setView(scroll)
            .setPositiveButton("OK", null)
            .show()
    }

    dialog.show()
    calculateKMap() // Initial calculation
}
