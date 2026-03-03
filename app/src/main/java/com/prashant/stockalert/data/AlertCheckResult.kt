package com.prashant.stockalert.data

sealed class AlertCheckResult {
    data class Success(val triggeredCount: Int) : AlertCheckResult()
    data class Failure(val reason: String) : AlertCheckResult()
}
