package com.jaewonlee.aidietrecord.data.repository

import com.jaewonlee.aidietrecord.data.ai.GeminiMealAnalyzer
import com.jaewonlee.aidietrecord.data.ai.MealAiAnalysis
import com.jaewonlee.aidietrecord.data.ai.MealAiFoodAnalysis
import com.jaewonlee.aidietrecord.data.model.MealFoodDraft
import com.jaewonlee.aidietrecord.data.model.MealFoodRecord
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.data.model.MealUploadDraft
import com.jaewonlee.aidietrecord.data.nutrition.estimateCaloriesFromFoodName
import com.jaewonlee.aidietrecord.data.nutrition.estimateHealthMetrics
import com.jaewonlee.aidietrecord.data.nutrition.estimateMacros
import java.util.Locale
import kotlin.math.max

class MealReviewRepository(
    private val geminiMealAnalyzer: GeminiMealAnalyzer? = null
) {
    suspend fun reviewMealDraft(draft: MealUploadDraft): MealRecord {
        val aiAnalysis = runCatching {
            geminiMealAnalyzer?.analyze(draft)
        }.getOrNull()

        val reviewInputs = buildReviewInputs(draft, aiAnalysis)
        val reviewedFoods = reviewInputs.mapIndexed { index, reviewInput ->
            val foodDraft = reviewInput.foodDraft
            val aiFood = reviewInput.aiFood
            val reviewedFoodName = aiFood?.foodName
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: foodDraft.foodName.trim().ifBlank { "Food ${index + 1}" }
            val estimationText = listOf(
                reviewedFoodName,
                foodDraft.description
            ).joinToString(" ").trim()
            val reviewedCalories = foodDraft.calories
                ?: aiFood?.calories
                ?: estimateCaloriesFromFoodName(estimationText)
            val estimatedMacros = estimateMacros(reviewedCalories)
            val estimatedHealthMetrics = estimateHealthMetrics(
                foodName = estimationText,
                calories = reviewedCalories
            )
            MealFoodRecord(
                foodName = reviewedFoodName,
                description = foodDraft.description.trim(),
                imageUri = foodDraft.imageUri,
                calories = reviewedCalories,
                carbsGram = foodDraft.carbsGram
                    ?: aiFood?.carbsGram
                    ?: estimatedMacros.carbsGram,
                proteinGram = foodDraft.proteinGram
                    ?: aiFood?.proteinGram
                    ?: estimatedMacros.proteinGram,
                fatGram = foodDraft.fatGram
                    ?: aiFood?.fatGram
                    ?: estimatedMacros.fatGram,
                fiberGram = foodDraft.fiberGram
                    ?: aiFood?.fiberGram
                    ?: estimatedHealthMetrics.fiberGram,
                sugarGram = foodDraft.sugarGram
                    ?: aiFood?.sugarGram
                    ?: estimatedHealthMetrics.sugarGram,
                sodiumMilligram = foodDraft.sodiumMilligram
                    ?: aiFood?.sodiumMilligram
                    ?: estimatedHealthMetrics.sodiumMilligram,
                aiFoodName = aiFood?.foodName ?: reviewedFoodName,
                aiCalories = aiFood?.calories ?: reviewedCalories,
                confidence = aiFood?.confidence ?: estimateConfidence(foodDraft)
            )
        }.mergeDuplicateFoods()

        return MealRecord(
            id = draft.id,
            ownerId = draft.ownerId,
            memo = draft.memo,
            imageUri = draft.imageUri ?: draft.foods.firstNotNullOfOrNull { it.imageUri },
            aiSummary = buildAiSummary(
                draft = draft,
                aiAnalysis = aiAnalysis,
                reviewedFoodCount = reviewedFoods.size
            ),
            createdAt = draft.createdAt,
            timeZoneId = draft.timeZoneId,
            localDateEpochDay = draft.localDateEpochDay,
            foods = reviewedFoods
        )
    }

    private fun buildReviewInputs(
        draft: MealUploadDraft,
        aiAnalysis: MealAiAnalysis?
    ): List<MealFoodReviewInput> {
        val aiFoods = aiAnalysis?.foods.orEmpty()
        if (aiFoods.isNotEmpty()) {
            val fallbackDraft = draft.foods.firstOrNull()
                ?: MealFoodDraft(
                    foodName = "",
                    description = draft.memo,
                    imageUri = draft.imageUri
                )
            return aiFoods.mapIndexed { index, aiFood ->
                MealFoodReviewInput(
                    foodDraft = draft.foods.getOrNull(index) ?: fallbackDraft,
                    aiFood = aiFood
                )
            }
        }

        return buildFallbackFoodDrafts(draft).map { foodDraft ->
            MealFoodReviewInput(foodDraft = foodDraft, aiFood = null)
        }
    }

    private fun buildFallbackFoodDrafts(draft: MealUploadDraft): List<MealFoodDraft> {
        val foodDrafts = draft.foods.takeIf { it.isNotEmpty() }
            ?: listOf(
                MealFoodDraft(
                    foodName = "",
                    description = draft.memo,
                    imageUri = draft.imageUri
                )
            )
        val singleFoodDraft = foodDrafts.singleOrNull() ?: return foodDrafts
        if (singleFoodDraft.hasManualNutrition()) {
            return foodDrafts
        }

        val mealText = listOf(
            singleFoodDraft.foodName,
            singleFoodDraft.description,
            draft.memo
        )
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
        val splitFoods = splitMealText(mealText)
        if (splitFoods.size <= 1) {
            return foodDrafts
        }

        return splitFoods.map { foodText ->
            singleFoodDraft.copy(
                foodName = foodText,
                description = foodText
            )
        }
    }

    private fun splitMealText(text: String): List<String> {
        val normalized = text
            .replace("\n", ",")
            .replace("，", ",")
            .replace("、", ",")
            .replace("·", ",")
            .replace("/", ",")
            .replace(" and ", ",", ignoreCase = true)
            .replace(" with ", ",", ignoreCase = true)
            .replace(" 그리고 ", ",")
            .replace(" 및 ", ",")
            .replace("랑 ", ",")
            .replace("와 ", ",")
            .replace("과 ", ",")

        return normalized
            .split(",", ";", "|", "+")
            .map { part ->
                part
                    .trim()
                    .trim('-', '•', '*')
                    .trim()
            }
            .filter { it.length >= 2 }
            .distinctBy { it.normalizedFoodKey() }
    }

    private fun List<MealFoodRecord>.mergeDuplicateFoods(): List<MealFoodRecord> {
        val mergedFoods = mutableListOf<MealFoodRecord>()
        forEach { food ->
            val duplicateIndex = mergedFoods.indexOfFirst { existingFood ->
                val existingKey = existingFood.foodName.normalizedFoodKey()
                val nextKey = food.foodName.normalizedFoodKey()
                existingKey.isNotBlank() && existingKey == nextKey
            }
            if (duplicateIndex < 0) {
                mergedFoods += food
            } else {
                mergedFoods[duplicateIndex] = mergedFoods[duplicateIndex].mergeWith(food)
            }
        }
        return mergedFoods
    }

    private fun MealFoodRecord.mergeWith(other: MealFoodRecord): MealFoodRecord {
        return copy(
            description = listOf(description, other.description)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(" / "),
            imageUri = imageUri ?: other.imageUri,
            calories = max(calories, other.calories),
            carbsGram = max(carbsGram, other.carbsGram),
            proteinGram = max(proteinGram, other.proteinGram),
            fatGram = max(fatGram, other.fatGram),
            fiberGram = max(fiberGram, other.fiberGram),
            sugarGram = max(sugarGram, other.sugarGram),
            sodiumMilligram = max(sodiumMilligram, other.sodiumMilligram),
            aiFoodName = aiFoodName ?: other.aiFoodName,
            aiCalories = listOfNotNull(aiCalories, other.aiCalories).maxOrNull(),
            confidence = listOfNotNull(confidence, other.confidence).maxOrNull()
        )
    }

    private fun buildAiSummary(
        draft: MealUploadDraft,
        aiAnalysis: MealAiAnalysis?,
        reviewedFoodCount: Int
    ): String {
        val aiSummary = aiAnalysis?.summary?.takeIf { it.isNotBlank() }
        if (aiSummary != null) {
            return aiSummary
        }

        val hasImage = draft.imageUri != null || draft.foods.any { it.imageUri != null }
        val imageText = if (hasImage) {
            "Gemini was unavailable, so local estimates were saved using the attached photo and notes."
        } else {
            "Gemini was unavailable, so local estimates were saved using the written notes."
        }
        val autoCalorieCount = draft.foods.count { it.calories == null }
            .coerceAtLeast(reviewedFoodCount)
        val calorieText = if (autoCalorieCount > 0) {
            " Calories were estimated for $autoCalorieCount item(s)."
        } else {
            ""
        }
        return "$imageText $calorieText"
    }

    private fun estimateConfidence(foodDraft: MealFoodDraft): Float {
        val macroInputCount = listOf(
            foodDraft.calories,
            foodDraft.carbsGram,
            foodDraft.proteinGram,
            foodDraft.fatGram,
            foodDraft.fiberGram,
            foodDraft.sugarGram,
            foodDraft.sodiumMilligram
        ).count { it != null }
        val hasContext = foodDraft.description.isNotBlank() || foodDraft.imageUri != null
        return when {
            macroInputCount >= 7 -> 0.94f
            macroInputCount >= 4 -> 0.92f
            hasContext -> 0.72f
            macroInputCount == 0 -> 0.68f
            else -> 0.82f
        }
    }

    private data class MealFoodReviewInput(
        val foodDraft: MealFoodDraft,
        val aiFood: MealAiFoodAnalysis?
    )
}

private fun MealFoodDraft.hasManualNutrition(): Boolean {
    return listOf(
        calories,
        carbsGram,
        proteinGram,
        fatGram,
        fiberGram,
        sugarGram,
        sodiumMilligram
    ).any { it != null }
}

private fun String.normalizedFoodKey(): String {
    return lowercase(Locale.ROOT)
        .filter { it.isLetterOrDigit() }
}
