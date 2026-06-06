package com.jaewonlee.aidietrecord.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.jaewonlee.aidietrecord.data.local.MealLogReminderSettings
import java.time.LocalDate
import java.time.ZoneId

object MealLogReminderScheduler {
    fun scheduleAll(
        context: Context,
        ownerId: Long,
        settings: MealLogReminderSettings
    ) {
        if (ownerId <= 0L) {
            cancelAll(context)
            return
        }
        scheduleOrCancel(context, ownerId, MealLogReminderType.Breakfast, settings.breakfastEnabled)
        scheduleOrCancel(context, ownerId, MealLogReminderType.Lunch, settings.lunchEnabled)
        scheduleOrCancel(context, ownerId, MealLogReminderType.Dinner, settings.dinnerEnabled)
    }

    fun scheduleNext(
        context: Context,
        ownerId: Long,
        type: MealLogReminderType
    ) {
        if (ownerId <= 0L) return
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAtMillis = nextTriggerAtMillis(type)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            reminderPendingIntent(context, ownerId, type, PendingIntent.FLAG_UPDATE_CURRENT)
                ?: return
        )
    }

    fun cancelAll(context: Context) {
        MealLogReminderType.entries.forEach { type ->
            cancel(context, type)
        }
    }

    fun cancel(context: Context, type: MealLogReminderType) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(
            reminderPendingIntent(context, ownerId = 0L, type, PendingIntent.FLAG_NO_CREATE)
                ?: return
        )
    }

    private fun scheduleOrCancel(
        context: Context,
        ownerId: Long,
        type: MealLogReminderType,
        enabled: Boolean
    ) {
        if (enabled) {
            scheduleNext(context, ownerId, type)
        } else {
            cancel(context, type)
        }
    }

    private fun nextTriggerAtMillis(type: MealLogReminderType): Long {
        val zoneId = ZoneId.systemDefault()
        val now = java.time.ZonedDateTime.now(zoneId)
        var trigger = LocalDate.now(zoneId)
            .atTime(type.alarmTime)
            .atZone(zoneId)
        if (!trigger.isAfter(now)) {
            trigger = trigger.plusDays(1)
        }
        return trigger.toInstant().toEpochMilli()
    }

    private fun reminderPendingIntent(
        context: Context,
        ownerId: Long,
        type: MealLogReminderType,
        flags: Int
    ): PendingIntent? {
        val intent = Intent(context, MealLogReminderReceiver::class.java)
            .setAction(ACTION_MEAL_LOG_REMINDER)
            .putExtra(EXTRA_OWNER_ID, ownerId)
            .putExtra(EXTRA_REMINDER_TYPE, type.name)
        return PendingIntent.getBroadcast(
            context,
            type.requestCode,
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    const val ACTION_MEAL_LOG_REMINDER = "com.jaewonlee.aidietrecord.MEAL_LOG_REMINDER"
    const val EXTRA_OWNER_ID = "ownerId"
    const val EXTRA_REMINDER_TYPE = "reminderType"
}
