package com.prashant.stockalert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.prashant.stockalert.data.local.AlertType
import com.prashant.stockalert.data.local.StockEntity

@Composable
fun WatchlistScreen(viewModel: StockViewModel) {

    val stocks by viewModel.stocks.collectAsState()

    var symbol by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }

    var stockForAlert by remember { mutableStateOf<StockEntity?>(null) }
    var stockForEdit by remember { mutableStateOf<StockEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        /* -------- ADD STOCK -------- */
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    label = { Text("Stock Symbol (e.g. TCS.BO)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = buyPrice,
                    onValueChange = { buyPrice = it },
                    label = { Text("Buy Price") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val price = buyPrice.toDoubleOrNull()
                        if (symbol.isNotBlank() && price != null) {
                            viewModel.addStock(symbol.trim(), price)
                            symbol = ""
                            buyPrice = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Stock")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        /* -------- STOCK LIST -------- */
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(stocks) { stock ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text(
                            stock.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            stock.symbol,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(4.dp))

                        Text("Buy price: ₹${stock.buyPrice}")

                        Spacer(Modifier.height(8.dp))

                        Row {
                            Button(
                                onClick = { stockForAlert = stock },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Add Alert")
                            }

                            OutlinedButton(
                                onClick = { stockForEdit = stock }
                            ) {
                                Text("Edit Buy Price")
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.deleteStock(stock) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete Stock")
                        }
                    }
                }
            }
        }
    }

    /* -------- ADD ALERT DIALOG -------- */
    stockForAlert?.let { stock ->
        AddAlertDialog(
            stockSymbol = stock.symbol,
            onDismiss = { stockForAlert = null },
            onSave = { type, target, recurring ->
                viewModel.addAlert(
                    symbol = stock.symbol,
                    type = type,
                    target = target,
                    recurring = recurring
                )
                stockForAlert = null
            }
        )
    }

    /* -------- EDIT BUY PRICE DIALOG -------- */
    stockForEdit?.let { stock ->
        EditBuyPriceDialog(
            stock = stock,
            onDismiss = { stockForEdit = null },
            onSave = { newPrice ->
                viewModel.updateBuyPrice(stock, newPrice)
                stockForEdit = null
            }
        )
    }
}

/* ================= ADD ALERT DIALOG ================= */

@Composable
fun AddAlertDialog(
    stockSymbol: String,
    onDismiss: () -> Unit,
    onSave: (AlertType, Double, Boolean) -> Unit
) {
    var selectedType by remember { mutableStateOf(AlertType.PRICE_UPPER) }
    var targetValue by remember { mutableStateOf("") }
    var recurring by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val target = targetValue.toDoubleOrNull()
                if (target != null) {
                    onSave(selectedType, target, recurring)
                }
            }) {
                Text("Save Alert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add Alert for $stockSymbol") },
        text = {
            Column {

                Text("Alert Type")

                Spacer(Modifier.height(4.dp))

                Row {
                    FilterChip(
                        selected = selectedType == AlertType.PRICE_UPPER,
                        onClick = { selectedType = AlertType.PRICE_UPPER },
                        label = { Text("Upper") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedType == AlertType.PRICE_LOWER,
                        onClick = { selectedType = AlertType.PRICE_LOWER },
                        label = { Text("Lower") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedType == AlertType.PERCENT_CHANGE,
                        onClick = { selectedType = AlertType.PERCENT_CHANGE },
                        label = { Text("% Change") }
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = {
                        Text(
                            if (selectedType == AlertType.PERCENT_CHANGE)
                                "Target % (use negative for loss)"
                            else
                                "Target Price"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
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

/* ================= EDIT BUY PRICE DIALOG ================= */

@Composable
fun EditBuyPriceDialog(
    stock: StockEntity,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var price by remember {
        mutableStateOf(stock.buyPrice.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val newPrice = price.toDoubleOrNull()
                if (newPrice != null) {
                    onSave(newPrice)
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
        title = { Text("Edit Buy Price") },
        text = {
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Buy Price") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
