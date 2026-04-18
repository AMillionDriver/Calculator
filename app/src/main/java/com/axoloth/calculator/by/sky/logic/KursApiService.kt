package com.axoloth.calculator.by.sky.logic

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface untuk mendefinisikan endpoint API Kurs dengan API Key dinamis.
 */
interface KursApiService {
    
    @GET("v6/{apiKey}/latest/{base}")
    fun getLatestRates(
        @Path("apiKey") apiKey: String,
        @Path("base") baseCurrency: String
    ): Call<ExchangeRateResponse>

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
