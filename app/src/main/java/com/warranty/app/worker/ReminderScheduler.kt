package com.warranty.app.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    fun ensureDailyWork() {
        val request = PeriodicWorkRequestBuilder<WarrantyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateInitialDelay())
            .build()
        workManager.enqueueUniquePeriodicWork(
            WarrantyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun triggerImmediateCheck() {
        val work = OneTimeWorkRequestBuilder<WarrantyReminderWorker>().build()
        workManager.enqueueUniqueWork(
            "-immediate",
            ExistingWorkPolicy.REPLACE,
            work
        )
    }

    private fun calculateInitialDelay(): Duration {
        val now = LocalDateTime.now()
        val targetTime = LocalDateTime.of(now.toLocalDate(), LocalTime.of(9, 0))
        val nextRun = if (now.isAfter(targetTime)) targetTime.plusDays(1) else targetTime
        return Duration.between(now, nextRun).coerceAtLeast(Duration.ZERO)
    }
}
