package com.jaewonlee.aidietrecord.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
    val createdAt: Long,
    val timeZoneId: String = currentTimeZoneId(),
    val localDateEpochDay: Long = localDateEpochDay(createdAt, timeZoneId)
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
    val description: String = "",
    val imageUri: String? = null,
    val calories: Int,
    val carbsGram: Int,
    val proteinGram: Int,
    val fatGram: Int,
    val fiberGram: Int = 0,
    val sugarGram: Int = 0,
    val sodiumMilligram: Int = 0,
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
    val timeZoneId: String = currentTimeZoneId(),
    val localDateEpochDay: Long = localDateEpochDay(createdAt, timeZoneId),
    val foods: List<MealFoodRecord>
) {
    val foodName: String
        get() = when {
            foods.isEmpty() -> "No food"
            foods.size == 1 -> foods.first().foodName
            else -> "${foods.first().foodName} + ${foods.size - 1} more"
        }

    val calories: Int
        get() = foods.sumOf { it.calories }

    val carbsGram: Int
        get() = foods.sumOf { it.carbsGram }

    val proteinGram: Int
        get() = foods.sumOf { it.proteinGram }

    val fatGram: Int
        get() = foods.sumOf { it.fatGram }

    val fiberGram: Int
        get() = foods.sumOf { it.fiberGram }

    val sugarGram: Int
        get() = foods.sumOf { it.sugarGram }

    val sodiumMilligram: Int
        get() = foods.sumOf { it.sodiumMilligram }

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
    val description: String = "",
    val imageUri: String? = null,
    val calories: Int,
    val carbsGram: Int,
    val proteinGram: Int,
    val fatGram: Int,
    val fiberGram: Int = 0,
    val sugarGram: Int = 0,
    val sodiumMilligram: Int = 0,
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
    val timeZoneId: String = currentTimeZoneId(),
    val localDateEpochDay: Long = localDateEpochDay(createdAt, timeZoneId),
    val foods: List<MealFoodDraft>
)

data class MealFoodDraft(
    val foodName: String,
    val description: String = "",
    val imageUri: String? = null,
    val calories: Int? = null,
    val carbsGram: Int? = null,
    val proteinGram: Int? = null,
    val fatGram: Int? = null,
    val fiberGram: Int? = null,
    val sugarGram: Int? = null,
    val sodiumMilligram: Int? = null
)

fun MealWithFoods.toMealRecord(): MealRecord {
    return MealRecord(
        id = meal.id,
        ownerId = meal.ownerId,
        memo = meal.memo,
        imageUri = meal.imageUri,
        aiSummary = meal.aiSummary,
        createdAt = meal.createdAt,
        timeZoneId = meal.timeZoneId,
        localDateEpochDay = meal.localDateEpochDay,
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
        createdAt = createdAt,
        timeZoneId = timeZoneId,
        localDateEpochDay = localDateEpochDay
    )
}

fun MealRecord.localDate(): LocalDate {
    return LocalDate.ofEpochDay(localDateEpochDay)
}

fun localDateEpochDay(createdAt: Long, timeZoneId: String): Long {
    return Instant.ofEpochMilli(createdAt)
        .atZone(zoneIdOrSystemDefault(timeZoneId))
        .toLocalDate()
        .toEpochDay()
}

fun currentTimeZoneId(): String {
    return ZoneId.systemDefault().id
}

fun zoneIdOrSystemDefault(timeZoneId: String): ZoneId {
    return runCatching { ZoneId.of(timeZoneId) }
        .getOrDefault(ZoneId.systemDefault())
}

fun MealFoodRecord.toMealFoodEntity(mealId: Long): MealFoodEntity {
    return MealFoodEntity(
        id = id,
        mealId = mealId,
        foodName = foodName,
        description = description,
        imageUri = imageUri,
        calories = calories,
        carbsGram = carbsGram,
        proteinGram = proteinGram,
        fatGram = fatGram,
        fiberGram = fiberGram,
        sugarGram = sugarGram,
        sodiumMilligram = sodiumMilligram,
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
        description = description,
        imageUri = imageUri,
        calories = calories,
        carbsGram = carbsGram,
        proteinGram = proteinGram,
        fatGram = fatGram,
        fiberGram = fiberGram,
        sugarGram = sugarGram,
        sodiumMilligram = sodiumMilligram,
        aiFoodName = aiFoodName,
        aiCalories = aiCalories,
        confidence = confidence
    )
}
