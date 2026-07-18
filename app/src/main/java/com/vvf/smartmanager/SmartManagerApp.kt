package com.vvf.smartmanager

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * VVF Smart Manager — Application entry point.
 *
 * Implements [Configuration.Provider] so Hilt-injected Workers (background indexing,
 * Phase 12) can be created by WorkManager via [HiltWorkerFactory].
 */
@HiltAndroidApp
class SmartManagerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
