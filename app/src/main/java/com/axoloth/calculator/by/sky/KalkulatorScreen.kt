package com.axoloth.calculator.by.sky

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

// Hapus : AppCompatActivity()
class KalkulatorScreen(private val activity: AppCompatActivity) {

    private lateinit var tvInput: TextView
    private lateinit var tvResult: TextView

    // Fungsi onCreate dihapus karena ini bukan Activity lagi

    fun render(parent: ViewGroup? = null): View {
        // Gunakan activity.layoutInflater
        val view = activity.layoutInflater.inflate(R.layout.ui_kalkulator, parent, false)

        tvInput = view.findViewById(R.id.tv_input)
        tvResult = view.findViewById(R.id.tv_result)

        val numericButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.btn_nolnol, R.id.btn_koma, R.id.btn_kurs, R.id.btn_weight, R.id.btn_settings
        )

        numericButtons.forEach { id ->
            view.findViewById<Button>(id).setOnClickListener { btn ->
                val text = (btn as Button).text.toString()
                // Gunakan activity sebagai context
                val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
                btn.startAnimation(anim)
                tvInput.append(text)
            }
        }

        view.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            it.startAnimation(anim)
            // Pastikan SettingsScreen juga diperbaiki polanya jika ia juga Activity
            val screen = SettingsScreen(activity).render()
            activity.setContentView(screen)
        }

        view.findViewById<Button>(R.id.btn_kurs).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            it.startAnimation(anim)
            Snackbar.make(
                view,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        view.findViewById<Button>(R.id.btn_weight).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            it.startAnimation(anim)
            Snackbar.make(
                view,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        view.findViewById<Button>(R.id.btn_c).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            it.startAnimation(anim)
            tvInput.text = ""
            tvResult.text = "0"
        }

        view.findViewById<Button>(R.id.btn_backspace).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            it.startAnimation(anim)
            val currentText = tvInput.text.toString()
            if (currentText.isNotEmpty()) {
                tvInput.text = currentText.dropLast(1)
            }
        }

        setupOperator(view)

        return view
    }

    private fun setupOperator(view: View) {
        val operators = mapOf(
            R.id.btn_tambah to "+",
            R.id.btn_kurang to "-",
            R.id.btn_kali to "*",
            R.id.btn_bagi to "/",
            R.id.btn_persen to "%"
        )

        operators.forEach { (id, symbol) ->
            view.findViewById<Button>(id).setOnClickListener {
                val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
                it.startAnimation(anim)
                tvInput.append(symbol)
            }
        }

        view.findViewById<Button>(R.id.btn_equal).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            it.startAnimation(anim)
            val input = tvInput.text.toString()
            tvResult.text = evaluateExpression(input)
        }
    }

    private fun evaluateExpression(expression: String): String {
        return try {
            val cleanedExpression = expression.replace("x", "*").replace(":", "/")
            val builder = net.objecthunter.exp4j.ExpressionBuilder(cleanedExpression).build()
            val result = builder.evaluate()
            val longResult = result.toLong()
            if (result == longResult.toDouble()) longResult.toString() else result.toString()
        } catch (_: Exception) {
            "Error"
        }
    }
}