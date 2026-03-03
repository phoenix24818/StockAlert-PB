package com.prashant.stockalert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_history")
data class AlertHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stockSymbol: String,
    val alertType: AlertType,
    val targetValue: Double,
    val triggerPrice: Double,
    val triggeredAt: Long
)
