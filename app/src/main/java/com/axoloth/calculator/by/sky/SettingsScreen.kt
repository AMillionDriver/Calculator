package com.axoloth.calculator.by.sky

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.google.android.material.snackbar.Snackbar

class SettingsScreen (val context: Context) {
    private lateinit var btn_account: Button
    private lateinit var btn_privacy: Button
    private lateinit var btn_policy: Button

    fun render(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.ui_settings, null, false)

        btn_account =view.findViewById(R.id.btn_account)
        btn_policy =view.findViewById(R.id.btn_policy)
        btn_privacy =view.findViewById(R.id.btn_privacy)

        setupLogic(view)
        return view
    }
    fun setupLogic(rootView: View) {
        btn_account.setOnClickListener {
            Snackbar.make(
                rootView,
                "This Feature Will Avaible Soon",
                Snackbar.LENGTH_SHORT
            ).show(
            )
        }
        btn_policy.setOnClickListener {
            Snackbar.make(
                rootView,
                "This Feature Will Avaible Soon",
                Snackbar.LENGTH_SHORT
            ).show(
            )
        }
        btn_privacy.setOnClickListener {
            Snackbar.make(
                rootView,
                "This Feature Will Avaible Soon",
                Snackbar.LENGTH_SHORT
            ).show(
            )
        }
    }
}