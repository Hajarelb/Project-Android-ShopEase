package com.mobile.shopease.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepository(private val dataStore: DataStore<Preferences>) {
    
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")

    val isNotificationsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[notificationsEnabledKey] ?: true
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[notificationsEnabledKey] = enabled
        }
    }
}
