package com.mobile.shopease.ui.screens

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.LocalizationPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalizationViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = LocalizationPreferences(application)

    val language: StateFlow<String> = prefs.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    val currency: StateFlow<String> = prefs.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "usd")

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            prefs.setLanguage(lang)
            // Apply language change
            val localeList = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    fun setCurrency(curr: String) {
        viewModelScope.launch {
            prefs.setCurrency(curr)
        }
    }
}
