package com.prashant.stockalert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Add
import androidx.compose.material3.icons.filled.History
import androidx.compose.material3.icons.filled.List
import androidx.compose.material3.icons.filled.Notifications
import androidx.compose.material3.icons.filled.Settings
import com.prashant.stockalert.ui.theme.StockAlertTheme
import com.prashant.stockalert.background.AlertScheduler
import com.prashant.stockalert.data.AlertRepository
import com.prashant.stockalert.data.StockRepository
import com.prashant.stockalert.data.local.AppDatabase
import com.prashant.stockalert.data.remote.ApiClient
import com.prashant.stockalert.ui.AdvancedScreen
import com.prashant.stockalert.ui.StockViewModel
import com.prashant.stockalert.ui.WatchlistScreen
import com.prashant.stockalert.ui.AlertsScreen
import com.prashant.stockalert.ui.HistoryScreen

// navigation helper
import androidx.compose.ui.graphics.vector.ImageVector


sealed class Screen(val title: String, val icon: ImageVector) {
    object Watchlist : Screen("Watchlist", Icons.Default.List)
    object Alerts : Screen("Alerts", Icons.Default.Notifications)
    object History : Screen("History", Icons.Default.History)
    object Advanced : Screen("Advanced", Icons.Default.Settings)

    companion object {
        fun values() = listOf(Watchlist, Alerts, History, Advanced)
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        // Room database
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "stocks.db"
        )
            .fallbackToDestructiveMigration()
            .build()

        // Repositories
        val stockRepository = StockRepository(
            db.stockDao(),
            ApiClient.api
        )

        val alertRepository = AlertRepository(
            db.alertDao(),
            db.stockDao(),
            db.alertHistoryDao(),
            ApiClient.api
        )



        // ViewModel (manual wiring, no DI)
        val viewModel = StockViewModel(
            stockRepository,
            alertRepository
        )



        // Start background monitoring (15 min, market hours only)
        AlertScheduler.start(this)

        // UI
        setContent {
            StockAlertTheme {
                // navigation state
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Watchlist) }
                // state for watchlist add dialog
                var showAddStock by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(title = { Text(currentScreen.title) })
                    },
                    bottomBar = {
                        NavigationBar {
                            Screen.values().forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        when (currentScreen) {
                            Screen.Watchlist -> {
                                FloatingActionButton(onClick = { showAddStock = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add stock")
                                }
                            }
                            else -> { /* no FAB */ }
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (currentScreen) {
                            Screen.Watchlist -> WatchlistScreen(
                                viewModel = viewModel,
                                showAdd = showAddStock,
                                onShowAddChange = { showAddStock = it }
                            )
                            Screen.Alerts -> AlertsScreen(viewModel)
                            Screen.History -> HistoryScreen(viewModel)
                            Screen.Advanced -> AdvancedScreen()
                        }
                    }
                }
            }
        }


    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MainScreenPreview() {
    StockAlertTheme {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Watchlist) }
        var showAddStock by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text(currentScreen.title) })
            },
            bottomBar = {
                NavigationBar {
                    Screen.values().forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentScreen == Screen.Watchlist) {
                    FloatingActionButton(onClick = { showAddStock = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentScreen) {
                    Screen.Watchlist -> WatchlistContent(
                        stocks = listOf(
                            com.prashant.stockalert.data.local.StockEntity("TCS.BO", "Tata Consultancy", 3000.0)
                        ),
                        onAddStock = { _, _ -> },
                        onAddAlert = {},
                        onEdit = {},
                        onDelete = {}
                    )
                    Screen.Alerts -> Text("Alerts content")
                    Screen.History -> Text("History content")
                    Screen.Advanced -> Text("Advanced content")
                }
            }
        }
    }
}
