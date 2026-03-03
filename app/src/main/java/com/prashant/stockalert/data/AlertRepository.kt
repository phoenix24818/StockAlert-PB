package com.prashant.stockalert.data

import android.util.Log
import com.prashant.stockalert.data.local.AlertDao
import com.prashant.stockalert.data.local.AlertEntity
import com.prashant.stockalert.data.local.AlertHistoryDao
import com.prashant.stockalert.data.local.AlertHistoryEntity
import com.prashant.stockalert.data.local.AlertType
import com.prashant.stockalert.data.local.StockDao
import com.prashant.stockalert.data.remote.YahooApi

data class TriggeredAlert(
    val symbol: String,
    val price: Double,
    val alert: AlertEntity
)

class AlertRepository(
    private val alertDao: AlertDao,
    private val stockDao: StockDao,
    private val historyDao: AlertHistoryDao,
    private val api: YahooApi
) {

    suspend fun checkAlerts(): Pair<AlertCheckResult, List<TriggeredAlert>> {

        val triggered = mutableListOf<TriggeredAlert>()

        return try {

            val alerts = alertDao.getActiveAlerts()

            for (alert in alerts) {

                val stock = stockDao.getStockBySymbol(alert.stockSymbol)
                    ?: continue

                val price = api.getChart(stock.symbol)
                    .chart.result
                    ?.firstOrNull()
                    ?.meta
                    ?.regularMarketPrice
                    ?: continue

                val hit = when (alert.type) {
                    AlertType.PRICE_UPPER -> price >= alert.targetValue
                    AlertType.PRICE_LOWER -> price <= alert.targetValue
                    AlertType.PERCENT_CHANGE -> {
                        val pct =
                            ((price - stock.buyPrice) / stock.buyPrice) * 100

                        if (alert.targetValue >= 0) {
                            pct >= alert.targetValue
                        } else {
                            pct <= alert.targetValue
                        }
                    }

                }

                if (hit) {

                    historyDao.insert(
                        AlertHistoryEntity(
                            stockSymbol = stock.symbol,
                            alertType = alert.type,
                            targetValue = alert.targetValue,
                            triggerPrice = price,
                            triggeredAt = System.currentTimeMillis()
                        )
                    )

                    triggered.add(
                        TriggeredAlert(stock.symbol, price, alert)
                    )

                    if (!alert.isRecurring) {
                        alertDao.update(alert.copy(triggered = true))
                    }
                }
            }

            AlertCheckResult.Success(triggered.size) to triggered

        } catch (e: Exception) {

            AlertCheckResult.Failure(
                e.message ?: "Network/API error"
            ) to emptyList()
        }
    }


    suspend fun insertAlert(alert: AlertEntity) {
        alertDao.insert(alert)
    }

    suspend fun getAllAlerts(): List<AlertEntity> {
        return alertDao.getAllAlerts()
    }

    suspend fun deleteAlert(alert: AlertEntity) {
        alertDao.delete(alert)
    }

    suspend fun updateAlert(alert: AlertEntity) {
        alertDao.update(alert)
    }

    suspend fun getHistory(): List<AlertHistoryEntity> {
        return historyDao.getAll()
    }


}
