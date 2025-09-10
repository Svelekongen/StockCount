package com.example.stockcount.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockcount.settings.SettingsStore
import com.example.stockcount.settings.UserSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsStore = SettingsStore(app)
    
    val settings: StateFlow<UserSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    fun updateHaptic(enabled: Boolean) = viewModelScope.launch {
        settingsStore.update { it.copy(hapticEnabled = enabled) }
    }

    fun updateSound(enabled: Boolean) = viewModelScope.launch {
        settingsStore.update { it.copy(soundEnabled = enabled) }
    }

    fun updateSeparator(separator: Char) = viewModelScope.launch {
        settingsStore.update { it.copy(csvSeparator = separator) }
    }

    fun updateFlashBehavior(behavior: UserSettings.FlashBehavior) = viewModelScope.launch {
        settingsStore.update { it.copy(flashBehavior = behavior) }
    }
}
