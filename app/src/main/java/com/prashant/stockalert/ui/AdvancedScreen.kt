package com.prashant.stockalert.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.prashant.stockalert.background.ManualCheckRunner
import com.prashant.stockalert.background.SanityTestRunner

@Composable
fun AdvancedScreen() {

    val context = LocalContext.current
    var sanityEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Advanced") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                "Advanced / Diagnostics",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(Modifier.weight(1f)) {
                        Text(
                            "Sanity Test Mode",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Test stock fetch & notification (TCS.BO)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Switch(
                        checked = sanityEnabled,
                        onCheckedChange = { enabled ->
                            sanityEnabled = enabled

                            if (enabled) {
                                SanityTestRunner.run(context) { success, message ->
                                    Toast.makeText(
                                        context,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // auto-reset
                                    sanityEnabled = false
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    ManualCheckRunner.run(context) { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check alerts now")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvancedPreview() {
    StockAlertTheme {
        AdvancedScreen()
    }
}
