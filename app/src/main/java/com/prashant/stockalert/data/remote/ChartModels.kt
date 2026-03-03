package com.prashant.stockalert.data.remote

data class ChartResponse(
    val chart: Chart
)

data class Chart(
    val result: List<ChartResult>?,
    val error: Any?
)

data class ChartResult(
    val meta: Meta
)

data class Meta(
    val symbol: String,
    val shortName: String?,
    val regularMarketPrice: Double?
)
