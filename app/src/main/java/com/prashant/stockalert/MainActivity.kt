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

            var selectedTab by remember { mutableStateOf(0) }

            Column {

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Watchlist") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Alerts") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("History") }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Advanced") }
                    )
                }

                when (selectedTab) {
                    0 -> WatchlistScreen(viewModel)
                    1 -> AlertsScreen(viewModel)
                    2 -> HistoryScreen(viewModel)
                    3 -> AdvancedScreen()
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
