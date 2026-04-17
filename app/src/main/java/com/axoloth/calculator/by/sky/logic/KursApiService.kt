package com.axoloth.calculator.by.sky.logic

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface untuk mendefinisikan endpoint API Kurs
 */
interface KursApiService {
    // Kita gunakan API Key "69b59e3341b31a316982404e" sebagai contoh (Atau ganti dengan milikmu)
    @GET("v6/69b59e3341b31a316982404e/latest/{base}")
    fun getLatestRates(@Path("base") baseCurrency: String): Call<ExchangeRateResponse>

    companion object {
        private const val BASE_URL = "https://v6.exchangerate-api.com/"

        fun create(): KursApiService {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(KursApiService::class.java)
        }
    }
}
