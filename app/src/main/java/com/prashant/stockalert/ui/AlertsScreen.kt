package com.prashant.stockalert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prashant.stockalert.data.local.AlertEntity
import com.prashant.stockalert.data.local.AlertType

@Composable
fun AlertsScreen(viewModel: StockViewModel) {

    LaunchedEffect(Unit) {
        viewModel.loadAlerts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(viewModel.alerts) { alert ->

                var showEdit by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            alert.stockSymbol,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            when (alert.type) {
                                AlertType.PRICE_UPPER ->
                                    "Trigger: Price ≥ ₹${alert.targetValue}"
                                AlertType.PRICE_LOWER ->
                                    "Trigger: Price ≤ ₹${alert.targetValue}"
                                AlertType.PERCENT_CHANGE ->
                                    "Trigger: % Change ≥ ${alert.targetValue}%"
                            }
                        )

                        Text(
                            if (alert.isRecurring) "Recurring" else "One-time",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(8.dp))

                        Row {
                            Button(
                                onClick = { showEdit = true },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Edit")
                            }

                            Button(
                                onClick = { viewModel.deleteAlert(alert) }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }

                if (showEdit) {
                    EditAlertDialog(
                        alert = alert,
                        onDismiss = { showEdit = false },
                        onSave = {
                            viewModel.updateAlert(it)
                            showEdit = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditAlertDialog(
    alert: AlertEntity,
    onDismiss: () -> Unit,
    onSave: (AlertEntity) -> Unit
) {
    var target by remember { mutableStateOf(alert.targetValue.toString()) }
    var recurring by remember { mutableStateOf(alert.isRecurring) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val value = target.toDoubleOrNull()
                if (value != null) {
                    onSave(
                        alert.copy(
                            targetValue = value,
                            isRecurring = recurring,
                            triggered = false
                        )
                    )
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Alert") },
        text = {
            Column {
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Value") }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recurring,
                        onCheckedChange = { recurring = it }
                    )
                    Text("Recurring")
                }
            }
        }
    )
}
