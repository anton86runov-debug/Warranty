package com.warranty.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.warranty.app.domain.model.WarrantyStatus
import com.warranty.app.domain.repository.WarrantyRepository
import com.warranty.app.domain.util.WarrantyStatusCalculator
import com.warranty.app.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class WarrantyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: WarrantyRepository,
    private val calculator: WarrantyStatusCalculator,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val items = repository.observe().first()
        items.filter { it.reminderEnabled }
            .map { item -> item to calculator.compute(item) }
            .filter { (_, computed) -> computed.daysRemaining != null }
            .forEach { (item, computed) ->
                val days = computed.daysRemaining ?: return@forEach
                if (days in REMINDER_DAYS && days >= 0 && computed.status != WarrantyStatus.EXPIRED) {
                    notificationHelper.showReminder(item, days)
                }
                if (computed.status == WarrantyStatus.EXPIRED && days == 0L) {
                    notificationHelper.showReminder(item, 0)
                }
            }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "warranty-reminder"
        private val REMINDER_DAYS = setOf(30L, 14L, 7L, 1L)
    }
}
