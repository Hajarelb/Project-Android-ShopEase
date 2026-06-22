package com.mobile.shopease.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalizationPreferences(private val context: Context) {
    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val CURRENCY_KEY = stringPreferencesKey("currency")
    }

    val language: Flow<String> = context.dataStore.data
        .map { it[LANGUAGE_KEY] ?: "en" }

    val currency: Flow<String> = context.dataStore.data
        .map { it[CURRENCY_KEY] ?: "usd" }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = lang }
    }

    suspend fun setCurrency(curr: String) {
        context.dataStore.edit { it[CURRENCY_KEY] = curr }
    }
}
