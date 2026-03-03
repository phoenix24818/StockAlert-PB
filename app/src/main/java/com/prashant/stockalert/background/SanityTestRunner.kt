package com.prashant.stockalert.background

import android.content.Context
import android.util.Log
import com.prashant.stockalert.data.remote.ApiClient
import com.prashant.stockalert.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SanityTestRunner {

    fun run(
        context: Context,
        onResult: (Boolean, String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.api.getChart("TCS.BO")

                val price = response.chart.result
                    ?.firstOrNull()
                    ?.meta
                    ?.regularMarketPrice

                if (price == null) {
                    onResult(false, "Price not received from API")
                    return@launch
                }

                NotificationHelper.show(
                    context,
                    "Sanity Test Passed",
                    "TCS.BO fetched successfully @ ₹$price"
                )

                onResult(true, "Sanity test successful")

            } catch (e: Exception) {
                Log.e("SanityTest", "Failed", e)
                onResult(false, e.message ?: "Unknown error")
            }
        }
    }
}
