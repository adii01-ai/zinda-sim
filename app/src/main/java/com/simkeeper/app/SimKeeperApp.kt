package com.simkeeper.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.simkeeper.app.data.repo.SimRepository
import com.simkeeper.app.reminders.NotificationHelper
import com.simkeeper.app.reminders.ReminderScheduler
import com.simkeeper.app.widget.WidgetUpdater
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SimKeeperApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var notificationHelper: NotificationHelper

    @Inject lateinit var reminderScheduler: ReminderScheduler

    @Inject lateinit var widgetUpdater: WidgetUpdater

    @Inject lateinit var simRepository: SimRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannels()
        appScope.launch { reminderScheduler.schedule() }
        appScope.launch {
            // Any change to SIM data: re-check reminders now and refresh widgets.
            simRepository.observeSims().distinctUntilChanged().collectLatest {
                delay(DATA_CHANGE_DEBOUNCE_MS)
                reminderScheduler.checkNow()
                widgetUpdater.updateAll()
            }
        }
    }

    private companion object {
        const val DATA_CHANGE_DEBOUNCE_MS = 500L
    }
}
