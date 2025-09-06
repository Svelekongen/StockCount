package com.example.stockcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.stockcount.data.ScanRepository
import com.example.stockcount.ui.ScanScreen
import com.example.stockcount.ui.theme.StockCountTheme

class MainActivity : ComponentActivity() {
    private val repository = ScanRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockCountTheme {
                ScanScreen(repository)
            }
        }
    }
}