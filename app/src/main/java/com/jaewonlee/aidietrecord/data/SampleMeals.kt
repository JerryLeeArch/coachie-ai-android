package com.jaewonlee.aidietrecord.data

import com.jaewonlee.aidietrecord.data.model.MealFoodRecord
import com.jaewonlee.aidietrecord.data.model.MealRecord
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

val sampleMeals = listOf(
    sampleMeal(
        id = 4,
        memo = "아침 식사. 포만감 있게 가볍게 시작",
        createdAt = timestampTodayOf(8, 15),
        foods = listOf(
            sampleFood("오트밀 바나나", 310, 54, 10, 7, "오트밀", 300)
        )
    ),
    sampleMeal(
        id = 5,
        memo = "점심 식사. 탄수화물과 단백질 균형",
        createdAt = timestampTodayOf(12, 35),
        foods = listOf(
            sampleFood("현미밥", 260, 56, 5, 2, "현미밥", 250),
            sampleFood("닭가슴살", 210, 0, 39, 5, "닭가슴살", 200),
            sampleFood("채소 반찬", 90, 6, 3, 7, "채소", 90)
        )
    ),
    sampleMeal(
        id = 6,
        memo = "운동 후 간식",
        createdAt = timestampTodayOf(16, 20),
        foods = listOf(
            sampleFood("프로틴 쉐이크", 220, 14, 28, 5, "프로틴 음료", 210)
        )
    ),
    sampleMeal(
        id = 1,
        memo = "점심 식사. 단백질 위주로 기록",
        createdAt = timestampOf(2026, 5, 13, 12, 20),
        foods = listOf(
            sampleFood("닭가슴살 샐러드", 420, 28, 38, 14, "샐러드", 390)
        )
    ),
    sampleMeal(
        id = 2,
        memo = "저녁 식사. 양이 많아서 칼로리를 조금 높게 입력",
        createdAt = timestampOf(2026, 5, 13, 18, 40),
        foods = listOf(
            sampleFood("김치볶음밥", 650, 86, 22, 24, "볶음밥", 610)
        )
    ),
    sampleMeal(
        id = 3,
        memo = "간식 기록",
        createdAt = timestampOf(2026, 5, 13, 9, 10),
        foods = listOf(
            sampleFood("그릭요거트", 180, 16, 18, 4, "요거트", 160)
        )
    )
)

private fun sampleMeal(
    id: Long,
    memo: String,
    createdAt: Long,
    foods: List<MealFoodRecord>
): MealRecord {
    return MealRecord(
        id = id,
        memo = memo,
        imageUri = null,
        aiSummary = "샘플 데이터로 구성된 식사 기록입니다.",
        createdAt = createdAt,
        foods = foods
    )
}

private fun sampleFood(
    foodName: String,
    calories: Int,
    carbsGram: Int,
    proteinGram: Int,
    fatGram: Int,
    aiFoodName: String,
    aiCalories: Int
): MealFoodRecord {
    return MealFoodRecord(
        foodName = foodName,
        calories = calories,
        carbsGram = carbsGram,
        proteinGram = proteinGram,
        fatGram = fatGram,
        aiFoodName = aiFoodName,
        aiCalories = aiCalories,
        confidence = 0.9f
    )
}

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
