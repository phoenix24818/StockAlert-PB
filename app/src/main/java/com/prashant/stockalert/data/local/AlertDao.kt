package com.prashant.stockalert.data.local

import androidx.room.*

@Dao
interface AlertDao {

    @Query("SELECT * FROM alerts WHERE stockSymbol = :symbol")
    suspend fun getAlertsForStock(symbol: String): List<AlertEntity>

    @Insert
    suspend fun insert(alert: AlertEntity)

    @Update
    suspend fun update(alert: AlertEntity)

    @Query("SELECT * FROM alerts WHERE triggered = 0 OR isRecurring = 1")
    suspend fun getActiveAlerts(): List<AlertEntity>

    @Query("SELECT * FROM alerts ORDER BY stockSymbol")
    suspend fun getAllAlerts(): List<AlertEntity>

    @Delete
    suspend fun delete(alert: AlertEntity)

}
