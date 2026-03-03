package com.prashant.stockalert.background

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object AlertScheduler {

    fun start(context: Context) {

        val work = PeriodicWorkRequestBuilder<AlertWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "STOCK_ALERT_WORK",
            ExistingPeriodicWorkPolicy.UPDATE,
            work
        )
    }
}
