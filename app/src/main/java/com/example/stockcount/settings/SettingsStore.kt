package com.example.stockcount.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_settings")

class SettingsStore(private val context: Context) {
    private object Keys {
        val HAPTIC = booleanPreferencesKey("haptic")
        val SOUND = booleanPreferencesKey("sound")
        val CSV_SEP = stringPreferencesKey("csv_sep")
        val FLASH = stringPreferencesKey("flash")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            hapticEnabled = prefs[Keys.HAPTIC] ?: true,
            soundEnabled = prefs[Keys.SOUND] ?: false,
            csvSeparator = (prefs[Keys.CSV_SEP] ?: ",").first(),
            flashBehavior = when (prefs[Keys.FLASH] ?: "MANUAL") {
                "AUTO" -> UserSettings.FlashBehavior.AUTO
                else -> UserSettings.FlashBehavior.MANUAL
            }
        )
    }

    suspend fun update(transform: (UserSettings) -> UserSettings) {
        val current = settings.first()
        val updated = transform(current)
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTIC] = updated.hapticEnabled
            prefs[Keys.SOUND] = updated.soundEnabled
            prefs[Keys.CSV_SEP] = updated.csvSeparator.toString()
            prefs[Keys.FLASH] = updated.flashBehavior.name
        }
    }
}


