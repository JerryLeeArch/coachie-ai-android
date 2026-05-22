package com.jaewonlee.aidietrecord.data.nutrition

data class EstimatedMacros(
    val carbsGram: Int,
    val proteinGram: Int,
    val fatGram: Int
)

data class EstimatedHealthMetrics(
    val fiberGram: Int,
    val sugarGram: Int,
    val sodiumMilligram: Int
)

fun estimateCaloriesFromFoodName(foodName: String): Int {
    val normalizedName = foodName.lowercase()
    return when {
        normalizedName.isBlank() -> 0
        normalizedName.contains("볶음밥") -> 650
        normalizedName.contains("비빔밥") -> 620
        normalizedName.contains("김밥") -> 480
        normalizedName.contains("라면") -> 500
        normalizedName.contains("샐러드") -> 390
        normalizedName.contains("닭가슴살") -> 220
        normalizedName.contains("도시락") -> 550
        normalizedName.contains("오트밀") -> 310
        normalizedName.contains("요거트") || normalizedName.contains("요구르트") -> 180
        normalizedName.contains("쉐이크") || normalizedName.contains("프로틴") -> 220
        normalizedName.contains("밥") -> 300
        normalizedName.contains("국") || normalizedName.contains("찌개") -> 260
        normalizedName.contains("빵") || normalizedName.contains("토스트") -> 300
        normalizedName.contains("커피") -> 80
        else -> 350
    }
}

fun estimateMacros(calories: Int): EstimatedMacros {
    return EstimatedMacros(
        carbsGram = ((calories * 0.45f) / 4f).toInt(),
        proteinGram = ((calories * 0.25f) / 4f).toInt(),
        fatGram = ((calories * 0.30f) / 9f).toInt()
    )
}

fun estimateHealthMetrics(foodName: String, calories: Int): EstimatedHealthMetrics {
    val normalizedName = foodName.lowercase()
    return when {
        normalizedName.isBlank() -> EstimatedHealthMetrics(0, 0, 0)
        normalizedName.contains("라면") -> EstimatedHealthMetrics(3, 5, 1700)
        normalizedName.contains("국") || normalizedName.contains("찌개") -> {
            EstimatedHealthMetrics(2, 4, 1400)
        }
        normalizedName.contains("김밥") -> EstimatedHealthMetrics(3, 5, 900)
        normalizedName.contains("비빔밥") -> EstimatedHealthMetrics(7, 8, 850)
        normalizedName.contains("볶음밥") -> EstimatedHealthMetrics(3, 4, 780)
        normalizedName.contains("샐러드") -> EstimatedHealthMetrics(6, 6, 360)
        normalizedName.contains("오트밀") -> EstimatedHealthMetrics(5, 9, 120)
        normalizedName.contains("요거트") || normalizedName.contains("요구르트") -> {
            EstimatedHealthMetrics(0, 12, 90)
        }
        normalizedName.contains("커피") -> EstimatedHealthMetrics(0, 8, 40)
        normalizedName.contains("닭가슴살") -> EstimatedHealthMetrics(0, 0, 430)
        normalizedName.contains("프로틴") || normalizedName.contains("쉐이크") -> {
            EstimatedHealthMetrics(1, 4, 260)
        }
        normalizedName.contains("밥") -> EstimatedHealthMetrics(2, 0, 20)
        normalizedName.contains("빵") || normalizedName.contains("토스트") -> {
            EstimatedHealthMetrics(3, 7, 420)
        }
        else -> EstimatedHealthMetrics(
            fiberGram = ((calories * 0.025f) / 4f).toInt().coerceAtLeast(1),
            sugarGram = ((calories * 0.04f) / 4f).toInt().coerceAtLeast(1),
            sodiumMilligram = 600
        )
    }
}
