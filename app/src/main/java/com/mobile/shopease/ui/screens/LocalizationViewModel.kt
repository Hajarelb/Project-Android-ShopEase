package com.mobile.shopease.ui.screens

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.ExchangeRatePreferences
import com.mobile.shopease.data.LocalizationPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalizationViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = LocalizationPreferences(application)
    private val ratePrefs = ExchangeRatePreferences(application)

    val language: StateFlow<String> = prefs.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val currency: StateFlow<String> = prefs.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "usd")

    val exchangeRates: StateFlow<Map<String, Double>> = ratePrefs.rates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), mapOf())

    fun convertPrice(priceUsd: Double, targetCurrency: String): Double {
        val rates = exchangeRates.value
        val rate = rates[targetCurrency] ?: 1.0
        return priceUsd * rate
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            prefs.setLanguage(lang)
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun setCurrency(curr: String) {
        viewModelScope.launch {
            prefs.setCurrency(curr)
        }
    }
}
