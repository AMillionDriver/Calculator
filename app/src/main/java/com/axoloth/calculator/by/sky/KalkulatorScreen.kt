package com.axoloth.calculator.by.sky

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class KalkulatorScreen : AppCompatActivity() {

    private lateinit var tvInput: TextView
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Langsung tampilkan hasil render ke layar Activity
        setContentView(render())
    }

    fun render(parent: ViewGroup? = null): View {
        // Gunakan layoutInflater milik Activity (bukan LayoutInflater.from(AppCompatActivity))
        val view = layoutInflater.inflate(R.layout.ui_kalkulator, parent)

        tvInput = view.findViewById(R.id.tv_input)
        tvResult = view.findViewById(R.id.tv_result)

        val numericButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.btn_nolnol, R.id.btn_koma, R.id.btn_back, R.id.btn_kurs, R.id.btn_weight, R.id.btn_settings
        )

        numericButtons.forEach { id ->
            view.findViewById<Button>(id).setOnClickListener { btn ->
                val text = (btn as Button).text.toString()
                val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
                btn.startAnimation(anim)
                tvInput.append(text)
            }
        }

//        for kurs, weight and settings
        view.findViewById<Button>(R.id.btn_settings).setOnClickListener {
            // Tukar tampilan MainActivity menjadi SettingsScreen
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_settings).startAnimation(anim)
            val screen = SettingsScreen(this). render()
            this.setContentView(screen)
        }
        view.findViewById<Button>(R.id.btn_kurs).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_kurs).startAnimation(anim)
            Snackbar.make(
                view,
                "This Feature Will Avaible Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        view.findViewById<Button>(R.id.btn_weight).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_weight).startAnimation(anim)
            Snackbar.make(
                view,
                "This Feature Will Avaible Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_back).startAnimation(anim)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        view.findViewById<Button>(R.id.btn_c).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_c).startAnimation(anim)
            tvInput.text = ""
            tvResult.text = "0"
        }

        view.findViewById<Button>(R.id.btn_backspace).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_backspace).startAnimation(anim)
            val currentText = tvInput.text.toString()
            if (currentText.isNotEmpty()) {
                tvInput.text = currentText.dropLast(1)
            }
        }

        setupOperator(view)

        return view
    }

    private fun setupOperator(view: View) {
        view.findViewById<Button>(R.id.btn_tambah).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_tambah).startAnimation(anim)
            tvInput.append("+")
        }
        view.findViewById<Button>(R.id.btn_kurang).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_kurang).startAnimation(anim)
            tvInput.append("-")
        }
        view.findViewById<Button>(R.id.btn_kali).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_kali).startAnimation(anim)
            tvInput.append("*")
        }
        view.findViewById<Button>(R.id.btn_bagi).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_bagi).startAnimation(anim)
            tvInput.append("/")
        }
        view.findViewById<Button>(R.id.btn_persen).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_persen).startAnimation(anim)
            tvInput.append("%")
        }
        view.findViewById<Button>(R.id.btn_equal).setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_click)
            view.findViewById<Button>(R.id.btn_equal).startAnimation(anim)
            val input = tvInput.text.toString()
            val result = evaluateExpression(input)
            tvResult.text = result
        }
    }

    private fun evaluateExpression(expression: String): String {
        return try {
            // Exp4j butuh simbol '*' untuk kali dan '/' untuk bagi// Jika di UI kamu pakai 'x' atau '÷', ganti dulu di sini
            val cleanedExpression = expression.replace("x", "*").replace(":", "/")

            val builder = net.objecthunter.exp4j.ExpressionBuilder(cleanedExpression).build()
            val result = builder.evaluate()

            // Jika hasilnya angka bulat (misal 5.0), hilangkan .0 nya jadi "5"
            val longResult = result.toLong()
            if (result == longResult.toDouble()) {
                longResult.toString()
            } else {
                result.toString()
            }
        } catch (_: Exception) {
            "Error"
        }
    }
}
