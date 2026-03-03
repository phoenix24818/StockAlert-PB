package com.prashant.stockalert.utils

import java.time.LocalTime

object MarketHours {

    private val start = LocalTime.of(9, 15)
    private val end = LocalTime.of(15, 30)

    fun isMarketOpen(): Boolean {
        val now = LocalTime.now()
        return now.isAfter(start) && now.isBefore(end)
    }
}
