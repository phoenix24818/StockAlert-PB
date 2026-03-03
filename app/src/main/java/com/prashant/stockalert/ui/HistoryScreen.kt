package com.prashant.stockalert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prashant.stockalert.data.local.AlertType
import com.prashant.stockalert.data.local.AlertHistoryEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: StockViewModel) {

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("History") }) }
    ) { padding ->
        HistoryContent(
            history = viewModel.history,
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        )
    }
}

@Composable
fun HistoryContent(
    history: List<AlertHistoryEntity>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (history.isEmpty()) {
            Text(
                "No history entries.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn {
                items(history) { item ->
                    HistoryListItem(item)
                }
            }
        }
    }
}

@Composable
fun HistoryListItem(item: AlertHistoryEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(item.stockSymbol, style = MaterialTheme.typography.titleMedium)
            Text(
                when (item.alertType) {
                    AlertType.PRICE_UPPER ->
                        "Price ≥ ₹${item.targetValue}"
                    AlertType.PRICE_LOWER ->
                        "Price ≤ ₹${item.targetValue}"
                    AlertType.PERCENT_CHANGE ->
                        "% Change ≥ ${item.targetValue}%"
                }
            )
            Text("Triggered at ₹${item.triggerPrice}")

            val date = remember(item.triggeredAt) {
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.triggeredAt))
            }

            Text(
                date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryPreview() {
    StockAlertTheme {
        HistoryContent(
            history = listOf(
                AlertHistoryEntity(
                    stockSymbol = "TCS.BO",
                    alertType = AlertType.PRICE_UPPER,
                    targetValue = 3000.0,
                    triggerPrice = 3050.0,
                    triggeredAt = System.currentTimeMillis()
                )
            )
        )
    }
}
