package com.axoloth.calculator.by.sky

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // Kirim token ke server aplikasi Anda.
        // Jika perlu mempertahankan token di sisi klien, simpan token ini dan tangani sesuai kebutuhan.
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Periksa apakah pesan mengandung payload data.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            // Proses data pesan di sini (misalnya, perbarui UI, mulai layanan)
        }

        // Periksa apakah pesan mengandung payload notifikasi.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // Tampilkan notifikasi khusus atau tangani di sini jika aplikasi di latar depan
        }
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implementasi logika untuk mengirim token ke server backend Anda
        Log.d(TAG, "sendRegistrationToServer($token)")
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
