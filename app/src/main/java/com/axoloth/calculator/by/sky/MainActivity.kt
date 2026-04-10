package com.axoloth.calculator.by.sky

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loginScreen = LoginScreen(this)
        setContentView(loginScreen.render())
    }

}
