package com.prashant.stockalert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stockSymbol: String,
    val type: AlertType,
    val targetValue: Double,
    val isRecurring: Boolean,
    val triggered: Boolean = false
)

enum class AlertType {
    PRICE_UPPER,
    PRICE_LOWER,
    PERCENT_CHANGE
}
