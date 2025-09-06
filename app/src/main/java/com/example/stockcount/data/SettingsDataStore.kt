package com.example.stockcount.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "settings"

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class SettingsDataStore(private val context: Context) {

    companion object {
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val FLASHLIGHT_ENABLED = booleanPreferencesKey("flashlight_enabled")
    }

    val vibrationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    val flashlightEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[FLASHLIGHT_ENABLED] ?: false
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setFlashlightEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[FLASHLIGHT_ENABLED] = enabled
        }
    }
}
