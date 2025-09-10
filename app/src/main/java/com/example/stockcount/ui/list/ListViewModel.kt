package com.example.stockcount.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockcount.data.CountRepository
import com.example.stockcount.data.entity.CountLine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ListViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CountRepository.get(app)
    private val query = MutableStateFlow("")
    val items: StateFlow<List<CountLine>> =
        query.flatMapLatest { q -> if (q.isBlank()) repo.observeAll() else repo.search(q) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { viewModelScope.launch { query.emit(q) } }
    
    fun deleteItem(ean: String) { viewModelScope.launch { repo.delete(ean) } }
}
