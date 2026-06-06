package com.jaewonlee.aidietrecord.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jaewonlee.aidietrecord.MainActivity
import com.jaewonlee.aidietrecord.R
import com.jaewonlee.aidietrecord.data.local.AuthSessionStore
import com.jaewonlee.aidietrecord.data.local.MealDatabase
import com.jaewonlee.aidietrecord.data.local.MealLogReminderSettingsStore
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking

class MealLogReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val ownerId = AuthSessionStore(appContext).getSavedUserId()
                val settings = MealLogReminderSettingsStore(appContext).load(ownerId)
                MealLogReminderScheduler.scheduleAll(appContext, ownerId, settings)
            }
            MealLogReminderScheduler.ACTION_MEAL_LOG_REMINDER -> {
                val ownerId = intent.getLongExtra(MealLogReminderScheduler.EXTRA_OWNER_ID, 0L)
                val type = MealLogReminderType.fromName(
                    intent.getStringExtra(MealLogReminderScheduler.EXTRA_REMINDER_TYPE)
                ) ?: return
                handleMealReminder(appContext, ownerId, type)
            }
        }
    }

    private fun handleMealReminder(
        context: Context,
        ownerId: Long,
        type: MealLogReminderType
    ) {
        if (ownerId <= 0L) return
        val settings = MealLogReminderSettingsStore(context).load(ownerId)
        if (!settings.isEnabled(type)) return

        val alreadyLogged = runBlocking {
            MealDatabase.getDatabase(context)
                .mealDao()
                .countMealsInLocalTimeRange(
                    ownerId = ownerId,
                    localDateEpochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay(),
                    startMillis = todayAtMillis(type.windowStart),
                    endMillis = todayAtMillis(type.windowEnd)
                ) > 0
        }

        if (!alreadyLogged) {
            showReminderNotification(context, type)
        }
        MealLogReminderScheduler.scheduleNext(context, ownerId, type)
    }

    private fun showReminderNotification(
        context: Context,
        type: MealLogReminderType
    ) {
        if (!notificationsAllowed(context)) return
        createNotificationChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_20)
            .setContentTitle("Time to log your ${type.label.lowercase()}")
            .setContentText(type.message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context)
            .notify(type.requestCode, notification)
    }

    private fun notificationsAllowed(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Meal log reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Gentle reminders to log breakfast, lunch, and dinner."
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun todayAtMillis(time: java.time.LocalTime): Long {
        val zoneId = ZoneId.systemDefault()
        return LocalDate.now(zoneId)
            .atTime(time)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    private fun com.jaewonlee.aidietrecord.data.local.MealLogReminderSettings.isEnabled(
        type: MealLogReminderType
    ): Boolean {
        return when (type) {
            MealLogReminderType.Breakfast -> breakfastEnabled
            MealLogReminderType.Lunch -> lunchEnabled
            MealLogReminderType.Dinner -> dinnerEnabled
        }
    }

    private companion object {
        const val CHANNEL_ID = "meal_log_reminders"
    }
}
