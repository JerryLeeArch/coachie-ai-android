package com.jaewonlee.aidietrecord.data

import com.jaewonlee.aidietrecord.data.model.MealRecord
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

val sampleMeals = listOf(
    MealRecord(
        id = 4,
        foodName = "오트밀 바나나",
        calories = 310,
        carbsGram = 54,
        proteinGram = 10,
        fatGram = 7,
        memo = "아침 식사. 포만감 있게 가볍게 시작",
        imageUri = null,
        aiFoodName = "오트밀",
        aiCalories = 300,
        createdAt = timestampTodayOf(8, 15)
    ),
    MealRecord(
        id = 5,
        foodName = "현미밥 닭가슴살 도시락",
        calories = 560,
        carbsGram = 62,
        proteinGram = 42,
        fatGram = 14,
        memo = "점심 식사. 탄수화물과 단백질 균형",
        imageUri = null,
        aiFoodName = "닭가슴살 도시락",
        aiCalories = 540,
        createdAt = timestampTodayOf(12, 35)
    ),
    MealRecord(
        id = 6,
        foodName = "프로틴 쉐이크",
        calories = 220,
        carbsGram = 14,
        proteinGram = 28,
        fatGram = 5,
        memo = "운동 후 간식",
        imageUri = null,
        aiFoodName = "프로틴 음료",
        aiCalories = 210,
        createdAt = timestampTodayOf(16, 20)
    ),
    MealRecord(
        id = 1,
        foodName = "닭가슴살 샐러드",
        calories = 420,
        carbsGram = 28,
        proteinGram = 38,
        fatGram = 14,
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
        carbsGram = 86,
        proteinGram = 22,
        fatGram = 24,
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
        carbsGram = 16,
        proteinGram = 18,
        fatGram = 4,
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

private fun timestampTodayOf(
    hour: Int,
    minute: Int
): Long {
    return LocalDate.now(ZoneId.of("Asia/Seoul"))
        .atTime(hour, minute)
        .atZone(ZoneId.of("Asia/Seoul"))
        .toInstant()
        .toEpochMilli()
}
