package com.example.stockcount.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScanRepository {
    private val _codes = MutableStateFlow<List<String>>(emptyList())
    val codes: StateFlow<List<String>> = _codes.asStateFlow()

    fun add(code: String) {
        _codes.value = _codes.value + code
    }

    fun remove(code: String) {
        _codes.value = _codes.value - code
    }
}
