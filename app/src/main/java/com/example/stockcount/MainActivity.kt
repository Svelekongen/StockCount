package com.example.stockcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stockcount.ui.theme.StockCountTheme

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scan : Screen("scan")
    object List : Screen("list")
    object Detail : Screen("detail")
    object Export : Screen("export")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockCountTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) { HomeScreen(navController) }
                    composable(Screen.Scan.route) { ScanScreen(navController) }
                    composable(Screen.List.route) { ListScreen(navController) }
                    composable(Screen.Detail.route) { DetailScreen(navController) }
                    composable(Screen.Export.route) { ExportScreen(navController) }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Home Screen")
        Button(onClick = { navController.navigate(Screen.Scan.route) }) {
            Text("Go to Scan")
        }
        Button(onClick = { navController.navigate(Screen.List.route) }) {
            Text("Go to List")
        }
        Button(onClick = { navController.navigate(Screen.Detail.route) }) {
            Text("Go to Detail")
        }
        Button(onClick = { navController.navigate(Screen.Export.route) }) {
            Text("Go to Export")
        }
    }
}

@Composable
fun ScanScreen(navController: NavController) {
    PlaceholderScreen("Scan Screen", navController)
}

@Composable
fun ListScreen(navController: NavController) {
    PlaceholderScreen("List Screen", navController)
}

@Composable
fun DetailScreen(navController: NavController) {
    PlaceholderScreen("Detail Screen", navController)
}

@Composable
fun ExportScreen(navController: NavController) {
    PlaceholderScreen("Export Screen", navController)
}

@Composable
private fun PlaceholderScreen(title: String, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title)
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    StockCountTheme {
        HomeScreen(rememberNavController())
    }
}

