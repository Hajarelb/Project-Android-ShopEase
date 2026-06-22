package com.mobile.shopease.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.shopease.data.UserPreferences
import com.mobile.shopease.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
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

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotifications(enabled) }
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Clear cart
                val cartRepo = com.mobile.shopease.data.repository.CartRepository()
                try { cartRepo.clearCart() } catch (e: Exception) { /* ignore if fails */ }

                // 2. Clear local preferences
                prefs.clearAll()

                // 3. Sign out from Supabase
                SupabaseClient.client.auth.signOut()

                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
