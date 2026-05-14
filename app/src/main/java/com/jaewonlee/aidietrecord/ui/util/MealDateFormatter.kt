package com.jaewonlee.aidietrecord.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val mealDateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.KOREAN)

fun formatMealDateTime(createdAt: Long): String {
    return Instant.ofEpochMilli(createdAt)
        .atZone(ZoneId.systemDefault())
        .format(mealDateTimeFormatter)
}
