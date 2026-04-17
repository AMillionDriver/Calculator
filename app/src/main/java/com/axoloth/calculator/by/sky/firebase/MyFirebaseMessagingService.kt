package com.axoloth.calculator.by.sky.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.axoloth.calculator.by.sky.MainActivity
import com.axoloth.calculator.by.sky.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Log Analytics: Notifikasi diterima
        val analytics = FirebaseAnalytics.getInstance(this)
        analytics.logEvent("notification_received", null)

        // 1. Ambil data dari Notification Payload (Pesan Standar)
        var title = remoteMessage.notification?.title
        var body = remoteMessage.notification?.body
        var imageUrl = remoteMessage.notification?.imageUrl?.toString()

        // 2. Ambil data dari Data Payload (Pesan Custom dari Server/Console)
        if (remoteMessage.data.isNotEmpty()) {
            if (title == null) title = remoteMessage.data["title"]
            if (body == null) body = remoteMessage.data["body"]
            if (imageUrl == null) imageUrl = remoteMessage.data["image"]
        }

        // Tampilkan Notifikasi
        sendNotification(
            title ?: "Kalkulator Pro",
            body ?: "Ada pesan baru untukmu!",
            imageUrl
        )
    }

    private fun sendNotification(title: String, messageBody: String, imageUrl: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Tambahkan data khusus jika ingin diarahkan ke screen tertentu
            putExtra("route", "notification_open")
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "default_notification_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Download gambar jika ada URL-nya
        val bitmap = if (!imageUrl.isNullOrEmpty()) getBitmapFromUrl(imageUrl) else null

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_icon_calculator_new)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Jika ada gambar, gunakan BigPictureStyle
        if (bitmap != null) {
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .setBigContentTitle(title)
                    .setSummaryText(messageBody)
            )
        } else {
            // Jika tidak ada gambar, gunakan BigTextStyle agar teks panjang tidak terpotong
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notifikasi Umum",
                NotificationManager.IMPORTANCE_HIGH // Ubah ke HIGH agar muncul popup (Heads-up)
            ).apply {
                description = "Channel untuk notifikasi umum aplikasi"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun getBitmapFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image: ${e.message}")
            null
        }
    }

    private fun sendRegistrationToServer(token: String?) {
        Log.d(TAG, "sendRegistrationToServer($token)")
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}