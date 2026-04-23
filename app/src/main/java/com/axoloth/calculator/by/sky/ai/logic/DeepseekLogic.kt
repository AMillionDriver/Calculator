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

object DeepseekLogic {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getAiExplanationStream(
        context: Context,
        expression: String,
        onChunk: (String) -> Unit,
        onError: (String, Int) -> Unit,
        onComplete: () -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val encryptedKey = remoteConfig.getString("Deepseek_AI")
            val apiKey = SimpleEncryption.decrypt(encryptedKey).trim()

            val url = "https://api.deepseek.com/chat/completions"
            
            val systemRules = com.axoloth.calculator.by.sky.ai.system.rules.ResponseRules.getSystemRules(java.util.Locale.getDefault().language)
            
            val jsonBody = JSONObject().apply {
                put("model", "deepseek-chat")
                put("messages", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemRules)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Explain this: $expression")
                    })
                })
                put("stream", true)
                put("max_tokens", 500)
                put("temperature", 0.1)
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    withContext(Dispatchers.Main) { onError("Deepseek Error: ${response.code}", response.code) }
                    return@withContext
                }

                val source: BufferedSource? = response.body?.source()
                source?.let { s ->
                    while (!s.exhausted()) {
                        val line = s.readUtf8Line() ?: continue
                        if (line.startsWith("data: ")) {
                            val data = line.substring(6)
                            if (data == "[DONE]") break
                            
                            try {
                                val jsonResponse = JSONObject(data)
                                val choices = jsonResponse.getJSONArray("choices")
                                if (choices.length() > 0) {
                                    val delta = choices.getJSONObject(0).getJSONObject("delta")
                                    if (delta.has("content")) {
                                        val content = delta.getString("content")
                                        withContext(Dispatchers.Main) { onChunk(content) }
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip partial/malformed JSON in stream
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) { onComplete() }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onError(e.message ?: "Deepseek Connection Error", -1) }
        }
    }
}
