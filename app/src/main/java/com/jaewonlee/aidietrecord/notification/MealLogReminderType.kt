package com.jaewonlee.aidietrecord.notification

import java.time.LocalTime

enum class MealLogReminderType(
    val requestCode: Int,
    val label: String,
    val alarmTime: LocalTime,
    val windowStart: LocalTime,
    val windowEnd: LocalTime,
    val message: String
) {
    Breakfast(
        requestCode = 1001,
        label = "Breakfast",
        alarmTime = LocalTime.of(8, 0),
        windowStart = LocalTime.of(4, 0),
        windowEnd = LocalTime.of(11, 0),
        message = "Start your morning with a quick meal log."
    ),
    Lunch(
        requestCode = 1002,
        label = "Lunch",
        alarmTime = LocalTime.of(12, 30),
        windowStart = LocalTime.of(11, 0),
        windowEnd = LocalTime.of(16, 0),
        message = "A small lunch check-in keeps your day on track."
    ),
    Dinner(
        requestCode = 1003,
        label = "Dinner",
        alarmTime = LocalTime.of(18, 30),
        windowStart = LocalTime.of(16, 0),
        windowEnd = LocalTime.MAX,
        message = "Wrap up gently with your dinner log."
    );

    companion object {
        fun fromName(name: String?): MealLogReminderType? {
            return entries.firstOrNull { it.name == name }
        }
    }
}
