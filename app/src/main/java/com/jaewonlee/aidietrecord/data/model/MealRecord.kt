package com.jaewonlee.aidietrecord.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "meals",
    indices = [Index(value = ["ownerId"])]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerId: Long = 0,
    val memo: String,
    val imageUri: String?,
    val aiSummary: String?,
    val createdAt: Long
)

@Entity(
    tableName = "meal_foods",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mealId"])]
)
data class MealFoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealId: Long,
    val foodName: String,
    val calories: Int,
    val carbsGram: Int,
    val proteinGram: Int,
    val fatGram: Int,
    val aiFoodName: String?,
    val aiCalories: Int?,
    val confidence: Float?
)

data class MealWithFoods(
    @Embedded val meal: MealEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val foods: List<MealFoodEntity>
)

data class MealRecord(
    val id: Long = 0,
    val ownerId: Long = 0,
    val memo: String,
    val imageUri: String?,
    val aiSummary: String?,
    val createdAt: Long,
    val foods: List<MealFoodRecord>
) {
    val foodName: String
        get() = when {
            foods.isEmpty() -> "음식 없음"
            foods.size == 1 -> foods.first().foodName
            else -> "${foods.first().foodName} 외 ${foods.size - 1}개"
        }

    val calories: Int
        get() = foods.sumOf { it.calories }

    val carbsGram: Int
        get() = foods.sumOf { it.carbsGram }

    val proteinGram: Int
        get() = foods.sumOf { it.proteinGram }

    val fatGram: Int
        get() = foods.sumOf { it.fatGram }

    val aiFoodName: String?
        get() = foods
            .map { it.aiFoodName ?: it.foodName }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(", ")

    val aiCalories: Int?
        get() = calories.takeIf { it > 0 }
}

data class MealFoodRecord(
    val id: Long = 0,
    val mealId: Long = 0,
    val foodName: String,
    val calories: Int,
    val carbsGram: Int,
    val proteinGram: Int,
    val fatGram: Int,
    val aiFoodName: String?,
    val aiCalories: Int?,
    val confidence: Float?
)

data class MealUploadDraft(
    val id: Long = 0,
    val ownerId: Long = 0,
    val memo: String,
    val imageUri: String?,
    val createdAt: Long,
    val foods: List<MealFoodDraft>
)

data class MealFoodDraft(
    val foodName: String,
    val calories: Int?,
    val carbsGram: Int?,
    val proteinGram: Int?,
    val fatGram: Int?
)

fun MealWithFoods.toMealRecord(): MealRecord {
    return MealRecord(
        id = meal.id,
        ownerId = meal.ownerId,
        memo = meal.memo,
        imageUri = meal.imageUri,
        aiSummary = meal.aiSummary,
        createdAt = meal.createdAt,
        foods = foods
            .sortedBy { it.id }
            .map { it.toMealFoodRecord() }
    )
}

fun MealRecord.toMealEntity(): MealEntity {
    return MealEntity(
        id = id,
        ownerId = ownerId,
        memo = memo,
        imageUri = imageUri,
        aiSummary = aiSummary,
        createdAt = createdAt
    )
}

fun MealFoodRecord.toMealFoodEntity(mealId: Long): MealFoodEntity {
    return MealFoodEntity(
        id = id,
        mealId = mealId,
        foodName = foodName,
        calories = calories,
        carbsGram = carbsGram,
        proteinGram = proteinGram,
        fatGram = fatGram,
        aiFoodName = aiFoodName,
        aiCalories = aiCalories,
        confidence = confidence
    )
}

private fun MealFoodEntity.toMealFoodRecord(): MealFoodRecord {
    return MealFoodRecord(
        id = id,
        mealId = mealId,
        foodName = foodName,
        calories = calories,
        carbsGram = carbsGram,
        proteinGram = proteinGram,
        fatGram = fatGram,
        aiFoodName = aiFoodName,
        aiCalories = aiCalories,
        confidence = confidence
    )
}
