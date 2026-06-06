package com.jaewonlee.aidietrecord.data.local

import android.content.Context

data class MealLogReminderSettings(
    val breakfastEnabled: Boolean = false,
    val lunchEnabled: Boolean = false,
    val dinnerEnabled: Boolean = false
)

class MealLogReminderSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun load(ownerId: Long): MealLogReminderSettings {
        if (ownerId <= 0L) return MealLogReminderSettings()
        return MealLogReminderSettings(
            breakfastEnabled = preferences.getBoolean(key(ownerId, KEY_BREAKFAST), false),
            lunchEnabled = preferences.getBoolean(key(ownerId, KEY_LUNCH), false),
            dinnerEnabled = preferences.getBoolean(key(ownerId, KEY_DINNER), false)
        )
    }

    fun save(ownerId: Long, settings: MealLogReminderSettings) {
        if (ownerId <= 0L) return
        preferences.edit()
            .putBoolean(key(ownerId, KEY_BREAKFAST), settings.breakfastEnabled)
            .putBoolean(key(ownerId, KEY_LUNCH), settings.lunchEnabled)
            .putBoolean(key(ownerId, KEY_DINNER), settings.dinnerEnabled)
            .apply()
    }

    private fun key(ownerId: Long, name: String): String {
        return "$ownerId.$name"
    }

    private companion object {
        const val PREFERENCES_NAME = "meal_log_reminder_settings"
        const val KEY_BREAKFAST = "breakfast_enabled"
        const val KEY_LUNCH = "lunch_enabled"
        const val KEY_DINNER = "dinner_enabled"
    }
}
