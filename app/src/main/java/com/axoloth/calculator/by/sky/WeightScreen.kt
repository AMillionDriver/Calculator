package com.axoloth.calculator.by.sky

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class WeightScreen (val activity: AppCompatActivity){
    private lateinit var btnBack: Button
    private lateinit var btnPanjang: Button
    private lateinit var btnArea: Button
    private lateinit var btnVolume: Button
    private lateinit var btnKecepatan: Button
    private lateinit var btnBerat: Button
    private lateinit var btnSuhu: Button
    private lateinit var btnDaya: Button
    private lateinit var btnTekanan: Button
    private lateinit var txtTitle : TextView

    fun render(parent: ViewGroup? = null): View {
        val view = LayoutInflater.from(activity).inflate(R.layout.ui_weight, parent, false)

        txtTitle = view.findViewById(R.id.txtTitle)
        btnBack = view.findViewById(R.id.btnBack)
        btnPanjang = view.findViewById(R.id.btnPanjang)
        btnArea = view.findViewById(R.id.btnArea)
        btnVolume = view.findViewById(R.id.btnVolume)
        btnKecepatan = view.findViewById(R.id.btnKecepatan)
        btnBerat = view.findViewById(R.id.btnBerat)
        btnSuhu = view.findViewById(R.id.btnSuhu)
        btnDaya = view.findViewById(R.id.btnDaya)
        btnTekanan = view.findViewById(R.id.btnTekanan)

        setupLogic(view)
        return view
    }
    fun setupLogic(rootView: View) {
        btnBack.setOnClickListener{
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnBack.startAnimation(anim)
            // Render ulang KalkulatorScreen ke MainActivity
            val kalkulatorView = KalkulatorScreen(activity).render()
            activity.setContentView(kalkulatorView)
        }
        btnPanjang.setOnClickListener{
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnPanjang.startAnimation(anim)
            // Render ke PanjangScreen
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        btnArea.setOnClickListener{
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnArea.startAnimation(anim)
            // Render ke AreaScreen
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        btnVolume.setOnClickListener{
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnVolume.startAnimation(anim)
            // Render ke VolumeScreen
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        btnKecepatan.setOnClickListener{
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnKecepatan.startAnimation(anim)
            // Render ke KecepatanScreen
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        btnBerat.setOnClickListener{
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnBerat.startAnimation(anim)
            // Render ke BeratScreen
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        btnSuhu.setOnClickListener{
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnSuhu.startAnimation(anim)
            // Render ke SuhuScreen
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        btnDaya.setOnClickListener{
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnDaya.startAnimation(anim)
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        btnTekanan.setOnClickListener{
            val anim = android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.button_click)
            btnTekanan.startAnimation(anim)
            Snackbar.make(
                rootView,
                "This Feature Will Available Soon",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        }
}