package com.prashant.stockalert.data.local

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [
        StockEntity::class,
        AlertEntity::class,
        AlertHistoryEntity::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun alertDao(): AlertDao
    abstract fun alertHistoryDao(): AlertHistoryDao
}


