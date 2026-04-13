package com.axoloth.calculator.by.sky

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val kalkulatorScreen = KalkulatorScreen(this )
        setContentView(kalkulatorScreen.render())
    }

}
