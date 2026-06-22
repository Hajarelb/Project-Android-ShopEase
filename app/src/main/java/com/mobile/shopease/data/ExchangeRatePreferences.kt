package com.mobile.shopease.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExchangeRatePreferences(private val context: Context) {
    companion object {
        val EUR_RATE_KEY = doublePreferencesKey("eur_rate")   // 1 USD = X EUR
        val GBP_RATE_KEY = doublePreferencesKey("gbp_rate")
        val MAD_RATE_KEY = doublePreferencesKey("mad_rate")
        val JPY_RATE_KEY = doublePreferencesKey("jpy_rate")
    }

    val rates: Flow<Map<String, Double>> = context.dataStore.data.map { prefs ->
        mapOf(
            "usd" to 1.0,
            "eur" to (prefs[EUR_RATE_KEY] ?: 0.92),    // today's rate
            "gbp" to (prefs[GBP_RATE_KEY] ?: 0.79),
            "mad" to (prefs[MAD_RATE_KEY] ?: 10.5),
            "jpy" to (prefs[JPY_RATE_KEY] ?: 149.5)
        )
    }
}
