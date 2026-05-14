package com.jaewonlee.aidietrecord.data

import com.jaewonlee.aidietrecord.data.model.MealRecord
import java.time.LocalDateTime
import java.time.ZoneId

val sampleMeals = listOf(
    MealRecord(
        id = 1,
        foodName = "닭가슴살 샐러드",
        calories = 420,
        memo = "점심 식사. 단백질 위주로 기록",
        imageUri = null,
        aiFoodName = "샐러드",
        aiCalories = 390,
        createdAt = timestampOf(2026, 5, 13, 12, 20)
    ),
    MealRecord(
        id = 2,
        foodName = "김치볶음밥",
        calories = 650,
        memo = "저녁 식사. 양이 많아서 칼로리를 조금 높게 입력",
        imageUri = null,
        aiFoodName = "볶음밥",
        aiCalories = 610,
        createdAt = timestampOf(2026, 5, 13, 18, 40)
    ),
    MealRecord(
        id = 3,
        foodName = "그릭요거트",
        calories = 180,
        memo = "간식 기록",
        imageUri = null,
        aiFoodName = "요거트",
        aiCalories = 160,
        createdAt = timestampOf(2026, 5, 13, 9, 10)
    )
)

private fun timestampOf(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int
): Long {
    return LocalDateTime.of(year, month, day, hour, minute)
        .atZone(ZoneId.of("Asia/Seoul"))
        .toInstant()
        .toEpochMilli()
}
