package com.axoloth.calculator.by.sky.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.constraintlayout.helper.widget.Flow
import com.axoloth.calculator.by.sky.R
import com.axoloth.calculator.by.sky.logic.setupKalkulatorLogic

class CalculatorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ui_kalkulator, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvInput: EditText = view.findViewById(R.id.tv_input)
        val tvResult: TextView = view.findViewById(R.id.tv_result)

        // Inisialisasi Adaptive Layout
        applyAdaptiveLayout(view)

        // Menginisialisasi logika kalkulator
        setupKalkulatorLogic(requireActivity() as AppCompatActivity, view, tvInput, tvResult)
    }

    private fun applyAdaptiveLayout(view: View) {
        val config = resources.configuration
        val screenWidthDp = config.screenWidthDp
        
        val flowMain = view.findViewById<Flow>(R.id.flowMain)
        val flowScientific = view.findViewById<Flow>(R.id.flowScientific)

        // Adaptive Logic: Gabungan Window Size Classes & Flow behavior
        if (screenWidthDp >= 600) {
            // Expanded / Landscape: Tampilkan lebih banyak kolom (FlowRow expanded)
            flowMain?.setMaxElementsWrap(5) 
            flowScientific?.setMaxElementsWrap(6)
        } else {
            // Compact: 4 Kolom standar
            flowMain?.setMaxElementsWrap(4)
            flowScientific?.setMaxElementsWrap(4)
        }
    }
}
