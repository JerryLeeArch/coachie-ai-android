package com.jaewonlee.aidietrecord.ui.util

import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.data.model.localDate
import com.jaewonlee.aidietrecord.data.model.zoneIdOrSystemDefault
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

fun formatMealDateTime(mealRecord: MealRecord): String {
    return Instant.ofEpochMilli(mealRecord.createdAt)
        .atZone(zoneIdOrSystemDefault(mealRecord.timeZoneId))
        .format(mealDateTimeFormatter)
}

fun formatMealDate(date: LocalDate): String {
    return date.format(mealDateFormatter)
}

fun mealRecordDate(mealRecord: MealRecord): LocalDate {
    return mealRecord.localDate()
}

fun mealRecordDate(createdAt: Long): LocalDate {
    return Instant.ofEpochMilli(createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun isTodayMeal(mealRecord: MealRecord): Boolean {
    return mealRecordDate(mealRecord) == LocalDate.now(ZoneId.systemDefault())
}
