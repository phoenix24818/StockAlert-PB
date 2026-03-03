package com.prashant.stockalert.data.remote

data class QuoteResponse(
    val quoteResponse: QuoteResult
)

data class QuoteResult(
    val result: List<Quote>
)

data class Quote(
    val symbol: String,
    val shortName: String,
    val regularMarketPrice: Double
)
