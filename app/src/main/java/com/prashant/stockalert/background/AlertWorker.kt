package com.prashant.stockalert.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.Room
import com.prashant.stockalert.data.AlertCheckResult
import com.prashant.stockalert.data.AlertRepository
import com.prashant.stockalert.data.local.AppDatabase
import com.prashant.stockalert.data.remote.ApiClient
import com.prashant.stockalert.notifications.NotificationHelper
import com.prashant.stockalert.utils.MarketHours

class AlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val ignoreMarketHours =
            inputData.getBoolean("IGNORE_MARKET_HOURS", false)

        if (!ignoreMarketHours && !MarketHours.isMarketOpen()) {
            return Result.success()
        }


        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "stocks.db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val repository = AlertRepository(
            db.alertDao(),
            db.stockDao(),
            db.alertHistoryDao(),
            ApiClient.api
        )


        val (result, triggered) = repository.checkAlerts()

        if (result is AlertCheckResult.Failure) {
            return Result.success()
        }

        triggered.forEach {
            NotificationHelper.show(
                applicationContext,
                "Stock Alert: ${it.symbol}",
                "Price hit ₹${it.price}"
            )
        }


        return Result.success()
    }
}
