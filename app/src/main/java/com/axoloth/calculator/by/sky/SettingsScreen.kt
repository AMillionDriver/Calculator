package com.axoloth.calculator.by.sky

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.snackbar.Snackbar

class SettingsScreen (val context: Context) {
    private lateinit var btnAccount: Button
    private lateinit var btnPrivacy: Button
    private lateinit var btnPolicy: Button

    fun render(parent: ViewGroup? = null): View {
        val view = LayoutInflater.from(context).inflate(R.layout.ui_settings, parent, false)

        btnAccount =view.findViewById(R.id.btnAccount)
        btnPrivacy =view.findViewById(R.id.btnPrivacy)
        btnPolicy =view.findViewById(R.id.btnPolicy)

        setupLogic(view)
        return view
    }
    fun setupLogic(rootView: View) {
        btnAccount.setOnClickListener {
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show(
            )
        }
        btnPrivacy.setOnClickListener {
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show(
            )
        }
        btnPolicy.setOnClickListener {
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show(
            )
        }
    }
}