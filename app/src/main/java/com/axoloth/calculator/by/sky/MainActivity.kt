package com.axoloth.calculator.by.sky

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        val kalkulatorScreen = KalkulatorScreen(this )
        setContentView(kalkulatorScreen.render())
    }

}
