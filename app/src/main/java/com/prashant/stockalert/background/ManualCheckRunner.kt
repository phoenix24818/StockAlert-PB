package com.prashant.stockalert.background

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf

object ManualCheckRunner {

    fun run(context: Context,
            onError: (String) -> Unit) {

        val request = OneTimeWorkRequestBuilder<AlertWorker>()
            .setInputData(
                workDataOf(
                    "IGNORE_MARKET_HOURS" to true
                )
            )
            .build()

        WorkManager.getInstance(context)
            .enqueue(request)

        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(request.id)
            .observeForever { info ->
                if (info?.state == WorkInfo.State.FAILED) {
                    onError("Failed to check alerts. Network issue.")
                }
            }

    }
}
