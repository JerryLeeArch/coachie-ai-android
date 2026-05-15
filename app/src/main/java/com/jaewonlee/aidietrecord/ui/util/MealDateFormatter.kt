package com.jaewonlee.aidietrecord.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val mealDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.KOREAN)

private val mealDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)

fun formatMealDateTime(createdAt: Long): String {
    return Instant.ofEpochMilli(createdAt)
        .atZone(ZoneId.systemDefault())
        .format(mealDateTimeFormatter)
}

fun formatMealDate(date: LocalDate): String {
    return date.format(mealDateFormatter)
}

fun mealRecordDate(createdAt: Long): LocalDate {
    return Instant.ofEpochMilli(createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun isTodayMeal(createdAt: Long): Boolean {
    return mealRecordDate(createdAt) == LocalDate.now(ZoneId.systemDefault())
}
