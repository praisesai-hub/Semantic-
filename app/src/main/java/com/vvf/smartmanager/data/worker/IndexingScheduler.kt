package com.vvf.smartmanager.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IndexingScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    /**
     * Runs at most every 6 hours, only when the device is idle and charging or has
     * plenty of battery — Master Specification v2.0, Section 11 (battery-friendly background).
     * No network constraint: indexing is a purely local filesystem scan.
     */
    fun schedulePeriodicIndexing() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<IndexingWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            IndexingWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
