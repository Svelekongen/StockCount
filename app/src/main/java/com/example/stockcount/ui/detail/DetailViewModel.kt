package com.example.stockcount.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockcount.data.CountRepository
import com.example.stockcount.data.entity.CountLine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class DetailViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CountRepository.get(app)

    fun loadItem(ean: String, callback: (CountLine?) -> Unit) = viewModelScope.launch {
        val item = repo.getItem(ean)
        callback(item)
    }

    fun save(line: CountLine) = viewModelScope.launch {
        repo.save(line.copy(updatedAt = Clock.System.now()))
    }

    fun delete(ean: String) = viewModelScope.launch { repo.delete(ean) }
}
