package com.axoloth.calculator.by.sky.ai.encryption

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object SimpleEncryption {
    private const val ALGORITHM = "AES"
    
    // Key sederhana untuk enkripsi lokal (bisa dikembangkan nanti)
    private val key = "AxolothSkyCalcAi".toByteArray() 

    fun decrypt(encryptedData: String): String {
        // 1. Bersihkan spasi/enter di awal dan akhir input dari Firebase
        val data = encryptedData.trim()
        if (data.isEmpty()) return ""

        // 2. Jika data mentah sudah diawali AIza, langsung balikin tanpa diproses
        if (data.startsWith("AIza")) {
            return data
        }

        return try {
            // 3. Bersihkan sisa-sisa karakter enter/spasi di tengah string (jika ada)
            val cleanedData = data.replace("\n", "").replace("\r", "").replace(" ", "")
            
            // 4. Decode menggunakan NO_WRAP agar tidak ada karakter tambahan
            val decodedBytes = Base64.decode(cleanedData, Base64.NO_WRAP)
            val result = String(decodedBytes).trim()
            
            // 5. Validasi hasil decode: Jika depannya AIza, berarti sukses
            if (result.startsWith("AIza")) result else data
        } catch (e: Exception) {
            data // Jika gagal decode, asumsikan ini key asli dan kembalikan apa adanya
        }
    }

    fun encrypt(data: String): String {
        // Gunakan NO_WRAP agar hasil enkripsi satu baris lurus tanpa 'enter'
        return Base64.encodeToString(data.toByteArray(), Base64.NO_WRAP).trim()
    }
}
