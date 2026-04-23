package com.axoloth.calculator.by.sky.ai.logic

import android.content.Context
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AiSwitcher {
    private const val TAG = "AiSwitcher"

    suspend fun getAiExplanationStream(
        context: Context,
        expression: String,
        isResume: Boolean = false,
        partialText: String = "",
        onChunk: (String, String) -> Unit,
        onError: (String) -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        // 1. Coba Gemini (Primary)
        val simulateError = false // SET TRUE UNTUK TESTING FALLBACK
        
        if (!simulateError && !isResume) {
            val geminiResult = CompletableDeferred<Boolean>()
            GeminiLogic.getAiExplanationStream(
                context,
                expression,
                isResume = false,
                partialText = "",
                onChunk = { chunk -> onChunk(chunk, "Gemini") },
                onError = { _, _ -> geminiResult.complete(false) },
                onComplete = { isTruncated -> geminiResult.complete(true) }
            )
            if (geminiResult.await()) {
                onComplete(false) // Asumsikan Gemini menangani truncated sendiri atau via resume
                return
            }
        }

        if (isResume) {
            // Jika sedang resume, kita hanya fokus ke Gemini
            GeminiLogic.getAiExplanationStream(
                context, expression, isResume = true, partialText = partialText,
                onChunk = { chunk -> onChunk(chunk, "Gemini") },
                onError = { msg, _ -> onError("Gagal melanjutkan: $msg") },
                onComplete = { isTruncated -> onComplete(isTruncated) }
            )
            return
        }

        // 2. Coba Deepseek (Backup 1)
        val deepseekResult = CompletableDeferred<Boolean>()
        try {
            DeepseekLogic.getAiExplanationStream(
                context,
                expression,
                onChunk = { chunk -> onChunk(chunk, "Deepseek") },
                onError = { _, _ -> deepseekResult.complete(false) },
                onComplete = { deepseekResult.complete(true) }
            )
        } catch (e: Exception) {
            deepseekResult.complete(false)
        }

        if (deepseekResult.await()) {
            onComplete(false)
            return
        }

        // 3. Coba Groq (Backup 2)
        val groqResult = CompletableDeferred<Boolean>()
        try {
            GroqLogic.getAiExplanationStream(
                context,
                expression,
                onChunk = { chunk -> onChunk(chunk, "Groq") },
                onError = { _, _ -> groqResult.complete(false) },
                onComplete = { groqResult.complete(true) }
            )
        } catch (e: Exception) {
            groqResult.complete(false)
        }

        if (groqResult.await()) {
            onComplete(false)
            return
        }

        // 4. Coba GPT (Backup 3 - Final)
        try {
            GptLogic.getAiExplanationStream(
                context,
                expression,
                onChunk = { chunk -> onChunk(chunk, "GPT") },
                onError = { msg, code ->
                    onError("Maaf, semua layanan AI sedang sibuk. Coba lagi nanti.")
                },
                onComplete = {
                    onComplete(false)
                }
            )
        } catch (e: Exception) {
            onError("Sistem AI sedang mengalami gangguan.")
        }
    }
}
