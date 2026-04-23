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
import kotlinx.coroutines.*
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
    val btnAi = view.findViewById<Button>(R.id.btnAi)
    val tvQuotaInfo = view.findViewById<TextView>(R.id.tv_quota_info)
    val btnRefill = view.findViewById<Button>(R.id.btn_refill_quota)

    fun updateQuotaUI() {
        val remaining = QuotaManager.getRemainingQuota(context)
        tvQuotaInfo.text = "Sisa Kuota AI: $remaining"
        
        if (remaining <= 0) {
            btnAi.isEnabled = false
            btnAi.alpha = 0.5f
            btnAi.text = "Kuota Habis! Silakan Refill"
        } else {
            btnAi.isEnabled = true
            btnAi.alpha = 1.0f
            btnAi.text = context.getString(R.string.jelaskan_dengan_ai)
        }

        if (QuotaManager.isCoolDown(context)) {
            val mins = QuotaManager.getRemainingCooldownMinutes(context)
            btnRefill.isEnabled = false
            btnRefill.text = "Pending ($mins m)"
        } else {
            btnRefill.isEnabled = true
            btnRefill.text = "+ Gratis Kuota"
        }
    }

    updateQuotaUI()
    com.axoloth.calculator.by.sky.ads.RewardedAdsLogic.loadRewardedAd(context as android.app.Activity)

    btnRefill.setOnClickListener {
        if (!com.axoloth.calculator.by.sky.ads.RewardedAdsLogic.isAdReady()) {
            android.widget.Toast.makeText(context, "Menyiapkan iklan...", android.widget.Toast.LENGTH_SHORT).show()
            com.axoloth.calculator.by.sky.ads.RewardedAdsLogic.loadRewardedAd(context as android.app.Activity)
            return@setOnClickListener
        }

        com.axoloth.calculator.by.sky.ads.RewardedAdsLogic.showRewardedAd(context as android.app.Activity) {
            updateQuotaUI()
            android.widget.Toast.makeText(context, "Kuota AI +5!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    btnAi.setOnClickListener {
        if (QuotaManager.getRemainingQuota(context) <= 0) {
            android.widget.Toast.makeText(context, "Kuota habis!", android.widget.Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        showAiTutorDialog(context, expression)
        // Kurangi kuota saat AI dialog dibuka
        QuotaManager.decrementQuota(context)
        updateQuotaUI()
    }

    btnClose.setOnClickListener {
        dialog.dismiss()
    }

    fun renderSteps(mode: String) {
        container.removeAllViews()
        val steps = generateSteps(context, expression, mode)
        steps.forEachIndexed { index, step ->
            val textView = TextView(context).apply {
                text = step
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setPadding(0, 12, 0, 12)
                setTextColor(context.getColor(android.R.color.white))
                
                // Logic styling berdasarkan prefix bahasa
                val isHeader = step.startsWith(context.getString(R.string.expl_step_1).substring(0, 4)) || 
                               step.startsWith(context.getString(R.string.expl_final_result_label)) || 
                               step.startsWith(context.getString(R.string.expl_simple_solution)) ||
                               step.startsWith(context.getString(R.string.expl_original_problem))
                
                if (isHeader) {
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

private var tts: android.speech.tts.TextToSpeech? = null

private fun showAiTutorDialog(context: Context, expression: String) {
    val aiDialog = BottomSheetDialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    val view = LayoutInflater.from(context).inflate(R.layout.layout_ai_tutor, null)
    aiDialog.setContentView(view)

    val tvContent = view.findViewById<TextView>(R.id.tv_ai_content)
    val btnClose = view.findViewById<View>(R.id.btn_close_ai)
    val btnDone = view.findViewById<Button>(R.id.btn_done_ai)
    val btnCopy = view.findViewById<Button>(R.id.btn_copy_ai)
    val btnSpeak = view.findViewById<android.widget.ImageButton>(R.id.btn_speak_ai)

    // Inisialisasi TTS
    tts = android.speech.tts.TextToSpeech(context) { status ->
        if (status == android.speech.tts.TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
        }
    }

    btnSpeak.setOnClickListener {
        val text = tvContent.text.toString()
        if (text.isNotEmpty() && !text.startsWith("🤖")) {
            tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    btnClose.setOnClickListener {
        tts?.stop()
        aiDialog.dismiss()
    }
    btnDone.setOnClickListener {
        tts?.stop()
        aiDialog.dismiss()
    }

    btnCopy.setOnClickListener {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("AI Explanation", tvContent.text)
        clipboard.setPrimaryClip(clip)
        android.widget.Toast.makeText(context, "Copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
    }

    // Panggil API Switcher Otomatis
    fun startAiStreaming(isResume: Boolean = false) {
        GlobalScope.launch(Dispatchers.Main) {
            val tvModel = view.findViewById<TextView>(R.id.tv_model_indicator)
            if (!isResume) {
                tvContent.text = "🤖 AI sedang memikirkan logika..."
                tvModel.visibility = View.GONE
            }
            view.findViewById<View>(R.id.error_layout)?.visibility = View.GONE
            view.findViewById<View>(R.id.resume_layout)?.visibility = View.GONE

            com.axoloth.calculator.by.sky.ai.logic.AiSwitcher.getAiExplanationStream(
                context, 
                expression,
                isResume = isResume,
                partialText = tvContent.text.toString(),
                onChunk = { chunk, modelName ->
                    if (tvContent.text.startsWith("🤖")) {
                        tvContent.text = ""
                        tvModel.text = "Dijelaskan oleh $modelName"
                        tvModel.visibility = View.VISIBLE
                        android.util.Log.d("ExplanationLogic", "Using Model: $modelName")
                    }
                    tvContent.append(chunk)
                },
                onError = { msg ->
                    view.findViewById<View>(R.id.error_layout)?.visibility = View.VISIBLE
                    val tvError = view.findViewById<TextView>(R.id.tv_error_msg)
                    tvError?.text = msg
                },
                onComplete = { isTruncated ->
                    if (isTruncated) {
                        view.findViewById<View>(R.id.resume_layout)?.visibility = View.VISIBLE
                    }
                }
            )
        }
    }

    view.findViewById<Button>(R.id.btn_resume_ai)?.setOnClickListener {
        view.findViewById<View>(R.id.resume_layout)?.visibility = View.GONE
        startAiStreaming(isResume = true)
    }

    startAiStreaming()
    aiDialog.show()
}

private fun generateSteps(context: Context, expression: String, mode: String): List<String> {
    val steps = mutableListOf<String>()
    val PI_VAL = "3.1415926535897932384626433832795028841971"
    
    // Normalisasi awal
    var currentExpr = expression.replace("×", "*").replace("÷", "/")
    steps.add("${context.getString(R.string.expl_original_problem)}:\n$expression")

    if (mode == "Shortway") {
        steps.add("${context.getString(R.string.expl_quick_trick)}:")
        steps.add(context.getString(R.string.expl_trick_1))
        steps.add(context.getString(R.string.expl_trick_2))
        val res = evaluateTerm(currentExpr)
        steps.add("\n✓ ${context.getString(R.string.expl_final_result_label)}: ${formatNum(res)}")
        return steps
    }

    // --- LANGKAH 1: HANDLING PI & CONSTANTS ---
    if (currentExpr.contains("π")) {
        steps.add(context.getString(R.string.expl_step_1))
        currentExpr = currentExpr.replace("π", "($PI_VAL)")
        steps.add("= $currentExpr")
    }

    // --- LANGKAH 2: PARENTHESES (KURUNG) ---
    if (currentExpr.contains("(")) {
        steps.add(context.getString(R.string.expl_step_2))
        val parenthesesRegex = Regex("\\(([^()]+)\\)")
        var match = parenthesesRegex.find(currentExpr)
        while (match != null) {
            val inside = match.groupValues[1]
            val result = evaluateTerm(inside)
            val oldExpr = "($inside)"
            currentExpr = currentExpr.replace(oldExpr, formatNum(result))
            steps.add("→ $oldExpr ${context.getString(R.string.expl_become)} ${formatNum(result)}")
            steps.add("= $currentExpr")
            match = parenthesesRegex.find(currentExpr)
        }
    }

    // --- LANGKAH 3: MULTIPLICATION / DIVISION (KALI / BAGI) ---
    if (currentExpr.contains("*") || currentExpr.contains("/")) {
        steps.add(context.getString(R.string.expl_step_3))
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
        steps.add(context.getString(R.string.expl_step_4))
        val finalResult = evaluateTerm(currentExpr)
        steps.add("= ${formatNum(finalResult)}")
    }

    // --- FINAL RESULT ---
    val finalRes = evaluateTerm(expression.replace("×", "*").replace("÷", "/"))
    steps.add("\n${context.getString(R.string.expl_final_result_label)}:\n${formatNum(finalRes)}")

    return if (mode == "Simple") {
        listOf("${context.getString(R.string.expl_simple_solution)}:", expression, "= ${formatNum(finalRes)}")
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
