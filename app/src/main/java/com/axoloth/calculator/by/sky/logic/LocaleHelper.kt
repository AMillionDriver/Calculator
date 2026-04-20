package com.axoloth.calculator.by.sky.logic

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun setLocale(context: Context, languageTag: String): Context {
        persist(context, languageTag)
        return updateResources(context, languageTag)
    }

    fun onAttach(context: Context): Context {
        val lang = getPersistedData(context, Locale.getDefault().toLanguageTag())
        return setLocale(context, lang)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        val preferences = context.getSharedPreferences("kalkulator_prefs", Context.MODE_PRIVATE)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    private fun persist(context: Context, language: String) {
        val preferences = context.getSharedPreferences("kalkulator_prefs", Context.MODE_PRIVATE)
        preferences.edit().putString(SELECTED_LANGUAGE, language).apply()
    }

    private fun updateResources(context: Context, languageTag: String): Context {
        // Menggunakan forLanguageTag agar mendukung region (misal: en-GB, it-CH)
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}
