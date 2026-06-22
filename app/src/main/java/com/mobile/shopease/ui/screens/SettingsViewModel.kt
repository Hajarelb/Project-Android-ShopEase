package com.mobile.shopease.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)

    val darkMode: StateFlow<Boolean> = prefs.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notifications: StateFlow<Boolean> = prefs.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val biometric: StateFlow<Boolean> = prefs.biometric
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotifications(enabled) }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch { prefs.setBiometric(enabled) }
    }
}
