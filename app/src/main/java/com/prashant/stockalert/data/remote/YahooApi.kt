package com.prashant.stockalert.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface YahooApi {

    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String
    ): ChartResponse
}
