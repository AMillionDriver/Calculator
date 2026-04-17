package com.axoloth.calculator.by.sky

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.axoloth.calculator.by.sky.screen.renderKalkulatorScreen
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private var backPressedOnce = false
    private var currentScreen = "Kalkulator" // Lacak screen aktif

    companion object {
        private const val TAG = "MainActivity"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen harus dipanggil sebelum super.onCreate
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Inisialisasi Tampilan
        setupUI()
        
        // Inisialisasi Layanan Background & Firebase dengan proteksi
        try {
            Firebase.initialize(context = this)
            Firebase.appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance(),
            )
            
            checkNotificationPermission()
            fetchFCMToken()
            setupBackNavigation()
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menginisialisasi layanan background", e)
            // Tetap jalankan navigasi back meskipun firebase gagal
            setupBackNavigation()
        }
    }

    private fun setupUI() {
        setContentView(renderKalkulatorScreen(this))
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, 
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun fetchFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Dapatkan token baru
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentScreen != "Kalkulator") {
                    // Jika bukan di kalkulator, balik ke kalkulator
                    currentScreen = "Kalkulator"
                    setContentView(renderKalkulatorScreen(this@MainActivity))
                } else {
                    // Jika di kalkulator, butuh 2x klik
                    if (backPressedOnce) {
                        finish()
                        return
                    }

                    backPressedOnce = true
                    Toast.makeText(this@MainActivity, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        backPressedOnce = false
                    }, 2000)
                }
            }
        })
    }

    // Fungsi helper untuk mengubah screen (dipanggil dari logic)
    fun updateCurrentScreen(screenName: String) {
        this.currentScreen = screenName
    }
}