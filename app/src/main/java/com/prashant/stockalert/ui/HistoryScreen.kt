package com.prashant.stockalert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prashant.stockalert.data.local.AlertType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: StockViewModel) {

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(viewModel.history) { item ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            item.stockSymbol,
                            style = MaterialTheme.typography.titleMedium
                        )

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
                            SimpleDateFormat(
                                "dd MMM yyyy, HH:mm",
                                Locale.getDefault()
                            ).format(Date(item.triggeredAt))
                        }

                        Text(
                            date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
