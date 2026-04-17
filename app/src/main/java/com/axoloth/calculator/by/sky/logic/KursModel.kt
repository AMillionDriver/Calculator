package com.axoloth.calculator.by.sky.logic

import com.google.gson.annotations.SerializedName

/**
 * Model data untuk respon dari ExchangeRate-API
 */
data class ExchangeRateResponse(
    @SerializedName("result") val result: String,
    @SerializedName("base_code") val baseCode: String,
    @SerializedName("conversion_rates") val conversionRates: Map<String, Double>
)
