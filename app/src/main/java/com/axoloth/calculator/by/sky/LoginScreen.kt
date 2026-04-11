package com.axoloth.calculator.by.sky

import android.content.Context
import android.content.Intent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kotlin.jvm.java

class LoginScreen(val context: Context) {
    private lateinit var emailform: EditText
    private lateinit var passwordform: EditText
    private lateinit var loginbutton: Button
    private lateinit var titletxt: TextView
    private lateinit var google: Button
    private lateinit var guest: Button
    private lateinit var apple: Button

    fun render(parent: ViewGroup? = null): View {
        val view = LayoutInflater.from(context).inflate(R.layout.ui_login, parent, false)

        emailform = view.findViewById(R.id.emailform)
        passwordform = view.findViewById(R.id.passwordform)
        loginbutton = view.findViewById(R.id.loginbutton)
        titletxt = view.findViewById(R.id.titletxt)
        google = view.findViewById(R.id.google)
        guest = view.findViewById(R.id.guest)
        apple = view.findViewById(R.id.apple)

        setupLogic(view)
        return view
    }
    fun setupLogic(rootView: View) {
        loginbutton.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.button_click)
            loginbutton.startAnimation(anim)
            val email = emailform.text.toString()
            val password = passwordform.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                println("Login berhasil")
            } else {
                println("Login gagal")
            }
            val intent = Intent(context, KalkulatorScreen::class.java)
            context.startActivity(intent)

        }
        guest.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.button_click)
            guest.startAnimation(anim)
            val intent = Intent(context, KalkulatorScreen::class.java)
            context.startActivity(intent)
        }
        google.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.button_click)
            google.startAnimation(anim)
            Snackbar.make(
                rootView,
                "This Feature Will Avaible Soon",
                Snackbar.LENGTH_SHORT
            ).show(
            )
        }
        apple.setOnClickListener {
            val anim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.button_click)
            apple.startAnimation(anim)
            Snackbar.make(
                rootView,
                "This Feature Will Avaible Soon",
                Snackbar.LENGTH_SHORT
            ).show(
            )
        }
    }


}