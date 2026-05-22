package com.jaewonlee.aidietrecord.data.repository

import com.jaewonlee.aidietrecord.data.ai.GeminiMealAnalyzer
import com.jaewonlee.aidietrecord.data.ai.MealAiAnalysis
import com.jaewonlee.aidietrecord.data.model.MealFoodDraft
import com.jaewonlee.aidietrecord.data.model.MealFoodRecord
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.data.model.MealUploadDraft
import com.jaewonlee.aidietrecord.data.nutrition.estimateCaloriesFromFoodName
import com.jaewonlee.aidietrecord.data.nutrition.estimateHealthMetrics
import com.jaewonlee.aidietrecord.data.nutrition.estimateMacros

class MealReviewRepository(
    private val geminiMealAnalyzer: GeminiMealAnalyzer? = null
) {
    suspend fun reviewMealDraft(draft: MealUploadDraft): MealRecord {
        val aiAnalysis = runCatching {
            geminiMealAnalyzer?.analyze(draft)
        }.getOrNull()

        val reviewedFoods = draft.foods.mapIndexed { index, foodDraft ->
            val aiFood = aiAnalysis?.foods?.getOrNull(index)
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
        }

        return MealRecord(
            id = draft.id,
            ownerId = draft.ownerId,
            memo = draft.memo,
            imageUri = draft.imageUri ?: draft.foods.firstNotNullOfOrNull { it.imageUri },
            aiSummary = buildAiSummary(draft, aiAnalysis),
            createdAt = draft.createdAt,
            foods = reviewedFoods
        )
    }

    private fun buildAiSummary(
        draft: MealUploadDraft,
        aiAnalysis: MealAiAnalysis?
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
}
