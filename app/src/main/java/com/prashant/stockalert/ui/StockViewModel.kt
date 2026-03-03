package com.prashant.stockalert.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prashant.stockalert.data.AlertRepository
import com.prashant.stockalert.data.StockRepository
import com.prashant.stockalert.data.local.AlertEntity
import com.prashant.stockalert.data.local.AlertType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.prashant.stockalert.data.local.AlertHistoryEntity
import com.prashant.stockalert.data.local.StockEntity

class StockViewModel(
    private val stockRepository: StockRepository,
    private val alertRepository: AlertRepository
) : ViewModel() {

    val stocks = stockRepository.getStocks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val alerts: SnapshotStateList<AlertEntity> = mutableStateListOf()

    fun addStock(symbol: String, buyPrice: Double) {
        viewModelScope.launch {
            stockRepository.addStock(symbol, buyPrice)
        }
    }

    // Phase-2: simple alert creation (upper price)
    fun addUpperPriceAlert(symbol: String, price: Double) {
        viewModelScope.launch {
            alertRepository.insertAlert(
                AlertEntity(
                    stockSymbol = symbol,
                    type = AlertType.PRICE_UPPER,
                    targetValue = price,
                    isRecurring = false
                )
            )
        }
    }

    fun addAlert(
        symbol: String,
        type: AlertType,
        target: Double,
        recurring: Boolean
    ) {
        viewModelScope.launch {
            alertRepository.insertAlert(
                AlertEntity(
                    stockSymbol = symbol,
                    type = type,
                    targetValue = target,
                    isRecurring = recurring
                )
            )
        }
    }

    fun loadAlerts() {
        viewModelScope.launch {
            alerts.clear()
            alerts.addAll(alertRepository.getAllAlerts())
        }
    }

    fun deleteAlert(alert: AlertEntity) {
        viewModelScope.launch {
            alertRepository.deleteAlert(alert)
            loadAlerts()
        }
    }

    fun updateAlert(alert: AlertEntity) {
        viewModelScope.launch {
            alertRepository.updateAlert(alert)
            loadAlerts()
        }
    }

    val history = mutableStateListOf<AlertHistoryEntity>()

    fun loadHistory() {
        viewModelScope.launch {
            history.clear()
            history.addAll(alertRepository.getHistory())
        }
    }

    fun updateBuyPrice(stock: StockEntity, newPrice: Double) {
        viewModelScope.launch {
            stockRepository.updateStock(
                stock.copy(buyPrice = newPrice)
            )
        }
    }

    fun deleteStock(stock: StockEntity) {
        viewModelScope.launch {
            stockRepository.deleteStock(stock)
        }
    }




//    fun testAlerts() {
//        viewModelScope.launch {
//            alertRepository.checkAlertsManually()
//        }
//    }
}
