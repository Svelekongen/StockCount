package com.example.stockcount.ui.export

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockcount.data.CountRepository
import com.example.stockcount.data.entity.CountLine
import com.example.stockcount.settings.SettingsStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExportViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CountRepository.get(app)
    private val settingsStore = SettingsStore(app)
    
    val items: StateFlow<List<CountLine>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    
    val settings: StateFlow<com.example.stockcount.settings.UserSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.example.stockcount.settings.UserSettings())

    fun updateSeparator(separator: Char) = viewModelScope.launch {
        settingsStore.update { it.copy(csvSeparator = separator) }
    }
}
