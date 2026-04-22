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
import okio.BufferedSource

object GeminiLogic {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getAiExplanationStream(
        context: Context,
        expression: String,
        isResume: Boolean = false,
        partialText: String = "",
        onChunk: (String) -> Unit,
        onError: (String, Int) -> Unit,
        onComplete: (Boolean) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val encryptedKey = remoteConfig.getString("Gemini_AI")
            val apiKey = SimpleEncryption.decrypt(encryptedKey).trim()

            // Kembali ke 2.5-flash karena terbukti lebih lancar di sisi user
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:streamGenerateContent?key=$apiKey"
            
            val systemRules = com.axoloth.calculator.by.sky.ai.system.rules.ResponseRules.getSystemRules(java.util.Locale.getDefault().language)
            val promptText = if (isResume) {
                "Lanjutkan jawaban ini: '$partialText'. Langsung sambung tanpa salam."
            } else {
                "$systemRules\n\nQuestion: $expression"
            }

            android.util.Log.d("GeminiLogic", "Sending request to: $url")

            val jsonBody = JSONObject().apply {
                put("contents", org.json.JSONArray().put(JSONObject().apply {
                    put("parts", org.json.JSONArray().put(JSONObject().apply {
                        put("text", promptText)
                    }))
                }))
                // Tambahkan config agar output tidak terpotong terlalu pendek
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("maxOutputTokens", 500)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    android.util.Log.e("GeminiLogic", "Response Failed: ${response.code} - $errorBody")
                    withContext(Dispatchers.Main) { onError("API Error: ${response.code}", response.code) }
                    return@withContext
                }

                val source: BufferedSource? = response.body?.source()
                var isTruncated = false

                source?.let { s ->
                    while (!s.exhausted()) {
                        val line = s.readUtf8Line() ?: continue
                        android.util.Log.d("GeminiLogic", "Raw Chunk: $line") // Intip data asli
                        
                        if (line.contains("\"text\": \"")) {
                            val chunk = line.substringAfter("\"text\": \"").substringBeforeLast("\"")
                            val cleanChunk = chunk
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\")
                                .replace("\\t", "\t")
                            
                            withContext(Dispatchers.Main) { onChunk(cleanChunk) }
                        }
                        
                        if (line.contains("\"finishReason\": \"MAX_TOKENS\"") || line.contains("\"finishReason\":\"MAX_TOKENS\"")) {
                            isTruncated = true
                        }
                    }
                }
                withContext(Dispatchers.Main) { onComplete(isTruncated) }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiLogic", "Error: ${e.message}", e)
            withContext(Dispatchers.Main) { onError(e.message ?: "Unknown Error", -1) }
        }
    }
}
