package com.axoloth.calculator.by.sky.ai.logic

import android.content.Context
import com.axoloth.calculator.by.sky.ai.encryption.SimpleEncryption
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiLogic {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getAiExplanation(context: Context, expression: String): String = withContext(Dispatchers.IO) {
        try {
            // 1. Safety Check (Client-side)
            if (!com.axoloth.calculator.by.sky.ai.system.prompt.SafetyPrompt.isSafe(expression)) {
                return@withContext com.axoloth.calculator.by.sky.ai.system.prompt.SafetyPrompt.REJECTION_MESSAGE
            }

            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val encryptedKey = remoteConfig.getString("Gemini_AI")
            
            if (encryptedKey.isEmpty()) return@withContext "Konfigurasi AI belum siap."
            
            val apiKey = SimpleEncryption.decrypt(encryptedKey).trim()

            // Menggunakan Gemini 2.0 Flash (Model terbaru & tercepat) lewat endpoint v1beta
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            
            val systemRules = com.axoloth.calculator.by.sky.ai.system.rules.ResponseRules.getSystemRules(java.util.Locale.getDefault().language)
            val promptText = "$systemRules\n\nQuestion: $expression"

            // Format JSON untuk Gemini 2.0
            val jsonBody = JSONObject().apply {
                put("contents", org.json.JSONArray().put(JSONObject().apply {
                    put("parts", org.json.JSONArray().put(JSONObject().apply {
                        put("text", promptText)
                    }))
                }))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2) // Rendah agar akurat tapi tetap natural
                    put("maxOutputTokens", 4096) // Kasih ruang sangat besar agar tidak terpotong
                    put("topP", 0.95)
                })
            }

            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseCode = response.code
            val responseBody = response.body?.string() ?: ""
            
            if (responseCode != 200) {
                // Mencoba mengambil pesan error detil dari JSON Google
                val errorMsg = try {
                    val errorJson = JSONObject(responseBody)
                    errorJson.getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "Unknown Error"
                }
                return@withContext "API Error ($responseCode): $errorMsg"
            }

            val jsonResponse = JSONObject(responseBody)
            
            if (jsonResponse.has("candidates")) {
                val candidates = jsonResponse.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.getJSONArray("parts")
                        return@withContext parts.getJSONObject(0).getString("text")
                    }
                }
            }
            
            "Maaf, AI sedang tidak dapat memberikan jawaban. Silakan coba pertanyaan lain."

        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
