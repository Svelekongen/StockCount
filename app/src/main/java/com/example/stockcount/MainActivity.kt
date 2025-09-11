package com.example.stockcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.example.stockcount.ui.scan.ScanScreen as CameraScanScreen
import com.example.stockcount.ui.list.ListScreen
import com.example.stockcount.ui.detail.DetailScreen
import com.example.stockcount.ui.export.ExportScreen
import com.example.stockcount.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scan : Screen("scan")
    object List : Screen("list")
    object Detail : Screen("detail/{ean}") {
        fun createRoute(ean: String) = "detail/$ean"
    }
    object Export : Screen("export")
    object Settings : Screen("settings")
    object Import : Screen("import")
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
                    composable(Screen.Scan.route) { CameraScanScreen(onBack = { navController.popBackStack() }) }
                    composable(Screen.List.route) { 
                        ListScreen(
                            onBack = { navController.popBackStack() },
                            onItemClick = { ean -> navController.navigate(Screen.Detail.createRoute(ean)) }
                        ) 
                    }
                    composable(Screen.Detail.route) { backStackEntry ->
                        val ean = backStackEntry.arguments?.getString("ean") ?: ""
                        DetailScreen(
                            ean = ean,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Export.route) { 
                        ExportScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Screen.Settings.route) { 
                        SettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Screen.Import.route) {
                        com.example.stockcount.ui.import.ImportScreen(onBack = { navController.popBackStack() })
                    }
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enkel Varetelling",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Rask og pålitelig varetelling for små butikker",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { navController.navigate(Screen.Scan.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start skanning")
        }
        Button(
            onClick = { navController.navigate(Screen.List.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se vareliste")
        }
        Button(
            onClick = { navController.navigate(Screen.Export.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Eksporter til CSV")
        }
        Button(
            onClick = { navController.navigate(Screen.Settings.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Innstillinger")
        }
        Button(
            onClick = { navController.navigate(Screen.Import.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Importer katalog")
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
fun DetailScreen(navController: NavController, ean: String) {
    PlaceholderScreen("Detail Screen for $ean", navController)
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

