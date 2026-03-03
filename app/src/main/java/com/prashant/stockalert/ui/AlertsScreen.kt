package com.prashant.stockalert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Delete
import androidx.compose.material3.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prashant.stockalert.data.local.AlertEntity
import com.prashant.stockalert.data.local.AlertType

@Composable
fun AlertsScreen(viewModel: StockViewModel) {

    LaunchedEffect(Unit) {
        viewModel.loadAlerts()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Alerts") })
        }
    ) { padding ->
        AlertsContent(
            alerts = viewModel.alerts,
            onUpdate = { viewModel.updateAlert(it) },
            onDelete = { viewModel.deleteAlert(it) },
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        )
    }
}

@Composable
fun AlertsContent(
    alerts: List<AlertEntity>,
    onUpdate: (AlertEntity) -> Unit,
    onDelete: (AlertEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (alerts.isEmpty()) {
            Text(
                "No alerts defined.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn {
                items(alerts) { alert ->
                    AlertListItem(
                        alert = alert,
                        onUpdate = onUpdate,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun AlertListItem(
    alert: AlertEntity,
    onUpdate: (AlertEntity) -> Unit,
    onDelete: (AlertEntity) -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(alert.stockSymbol, style = MaterialTheme.typography.titleMedium)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showEdit = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = { onDelete(alert) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }

    if (showEdit) {
        EditAlertDialog(
            alert = alert,
            onDismiss = { showEdit = false },
            onSave = {
                onUpdate(it)
                showEdit = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlertsPreview() {
    StockAlertTheme {
        AlertsContent(
            alerts = listOf(
                AlertEntity(
                    stockSymbol = "TCS.BO",
                    type = AlertType.PRICE_UPPER,
                    targetValue = 3000.0,
                    isRecurring = false
                ),
                AlertEntity(
                    stockSymbol = "INFY.BO",
                    type = AlertType.PERCENT_CHANGE,
                    targetValue = 5.0,
                    isRecurring = true
                )
            ),
            onUpdate = {},
            onDelete = {}
        )
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
