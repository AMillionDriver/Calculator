package com.axoloth.calculator.by.sky

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class KalkulatorScreen : AppCompatActivity() {

    private lateinit var tvInput: TextView
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Langsung tampilkan hasil render ke layar Activity
        setContentView(render())
    }

    fun render(): View {
        // Gunakan layoutInflater milik Activity (bukan LayoutInflater.from(AppCompatActivity))
        val view = layoutInflater.inflate(R.layout.ui_kalkulator, null)

        tvInput = view.findViewById(R.id.tv_input)
        tvResult = view.findViewById(R.id.tv_result)

        val numericButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.btn_nolnol, R.id.btn_koma, R.id.btn_back
        )

        numericButtons.forEach { id ->
            view.findViewById<Button>(id).setOnClickListener { btn ->
                val text = (btn as Button).text.toString()
                tvInput.append(text)
            }
        }
        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }
        view.findViewById<Button>(R.id.btn_c).setOnClickListener {
            tvInput.text = ""
            tvResult.text = "0"
        }

        view.findViewById<Button>(R.id.btn_backspace).setOnClickListener {
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
            tvInput.append("+")
        }
        view.findViewById<Button>(R.id.btn_kurang).setOnClickListener {
            tvInput.append("-")
        }
        view.findViewById<Button>(R.id.btn_kali).setOnClickListener {
            tvInput.append("*")
        }
        view.findViewById<Button>(R.id.btn_bagi).setOnClickListener {
            tvInput.append("/")
        }
        view.findViewById<Button>(R.id.btn_persen).setOnClickListener {
            tvInput.append("%")
        }
        view.findViewById<Button>(R.id.btn_equal).setOnClickListener {
            val input = tvInput.text.toString()
            val result = evaluateExpression(input)
            tvResult.text = result.toString()
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
        } catch (e: Exception) {
            "Error"
        }
    }
}
