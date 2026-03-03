package com.prashant.stockalert.data

import com.prashant.stockalert.data.local.StockDao
import com.prashant.stockalert.data.local.StockEntity
import com.prashant.stockalert.data.remote.YahooApi
import android.util.Log


class StockRepository(
    private val dao: StockDao,
    private val api: YahooApi
) {

    fun getStocks() = dao.getAllStocks()

    suspend fun addStock(symbol: String, buyPrice: Double) {

        try {
            Log.d("StockRepository", "Requesting chart for $symbol")

            val response = api.getChart(symbol)

            val meta = response.chart.result
                ?.firstOrNull()
                ?.meta

            if (meta == null || meta.regularMarketPrice == null) {
                Log.e("StockRepository", "No valid data for $symbol")
                return
            }

            Log.d(
                "StockRepository",
                "Chart data -> ${meta.symbol}, ${meta.shortName}, ${meta.regularMarketPrice}"
            )

            dao.insert(
                StockEntity(
                    symbol = meta.symbol,
                    name = meta.shortName ?: meta.symbol,
                    buyPrice = buyPrice
                )
            )

        } catch (e: Exception) {
            Log.e("StockRepository", "Chart API error for $symbol", e)
        }
    }

    suspend fun updateStock(stock: StockEntity) {
        dao.update(stock)
    }

    suspend fun deleteStock(stock: StockEntity) {
        dao.delete(stock)
    }




}
