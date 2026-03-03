package com.prashant.stockalert.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Add
import androidx.compose.material3.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.prashant.stockalert.data.local.AlertType
import com.prashant.stockalert.data.local.StockEntity

@Composable
fun WatchlistScreen(
    viewModel: StockViewModel,
    showAdd: Boolean,
    onShowAddChange: (Boolean) -> Unit
) {
    val stocks by viewModel.stocks.collectAsState()

    var stockForAlert by remember { mutableStateOf<StockEntity?>(null) }
    var stockForEdit by remember { mutableStateOf<StockEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Watchlist") })
        }
    ) { padding ->
        WatchlistContent(
            stocks = stocks,
            onAddStock = { symbol, price -> viewModel.addStock(symbol, price) },
            onAddAlert = { stockForAlert = it },
            onEdit = { stockForEdit = it },
            onDelete = { viewModel.deleteStock(it) },
            modifier = Modifier.padding(padding).padding(16.dp)
        )
    }

    if (showAdd) {
        AddStockDialog(
            onAdd = { symbol, price ->
                viewModel.addStock(symbol, price)
                onShowAddChange(false)
            },
            onDismiss = { onShowAddChange(false) }
        )
    }

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

@Composable
fun WatchlistContent(
    stocks: List<StockEntity>,
    onAddStock: (String, Double) -> Unit,
    onAddAlert: (StockEntity) -> Unit,
    onEdit: (StockEntity) -> Unit,
    onDelete: (StockEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (stocks.isEmpty()) {
            Text(
                "No stocks added yet. Tap + to add.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn {
                items(stocks) { stock ->
                    StockListItem(
                        stock = stock,
                        onAddAlert = { onAddAlert(stock) },
                        onEdit = { onEdit(stock) },
                        onDelete = { onDelete(stock) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WatchlistPreview() {
    StockAlertTheme {
        WatchlistContent(
            stocks = listOf(
                StockEntity(symbol = "TCS.BO", name = "Tata Consultancy", buyPrice = 3100.0),
                StockEntity(symbol = "INFY.BO", name = "Infosys", buyPrice = 1250.0)
            ),
            onAddStock = { _, _ -> },
            onAddAlert = {},
            onEdit = {},
            onDelete = {}
        )
    }
}

@Composable
fun StockListItem(
    stock: StockEntity,
    onAddAlert: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stock.name, style = MaterialTheme.typography.titleMedium)
            Text(
                stock.symbol,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text("Buy price: ₹${stock.buyPrice}")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onAddAlert) { Text("Alert") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onEdit) { Text("Edit") }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun AddStockDialog(
    onAdd: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var symbol by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add stock") },
        text = {
            Column {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    label = { Text("Symbol") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Buy price") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = price.toDoubleOrNull()
                if (symbol.isNotBlank() && p != null) {
                    onAdd(symbol.trim(), p)
                }
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
