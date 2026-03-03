package com.prashant.stockalert.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AlertHistoryDao {

    @Insert
    suspend fun insert(history: AlertHistoryEntity)

    @Query("SELECT * FROM alert_history ORDER BY triggeredAt DESC")
    suspend fun getAll(): List<AlertHistoryEntity>
}
