package com.jaewonlee.aidietrecord.data.repository

import com.jaewonlee.aidietrecord.data.model.MealFoodDraft
import com.jaewonlee.aidietrecord.data.model.MealFoodRecord
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.data.model.MealUploadDraft
import com.jaewonlee.aidietrecord.data.nutrition.estimateCaloriesFromFoodName
import com.jaewonlee.aidietrecord.data.nutrition.estimateMacros

class MealReviewRepository {
    suspend fun reviewMealDraft(draft: MealUploadDraft): MealRecord {
        val reviewedFoods = draft.foods.map { foodDraft ->
            val reviewedCalories = foodDraft.calories
                ?: estimateCaloriesFromFoodName(foodDraft.foodName)
            val estimatedMacros = estimateMacros(reviewedCalories)
            MealFoodRecord(
                foodName = foodDraft.foodName.trim(),
                calories = reviewedCalories,
                carbsGram = foodDraft.carbsGram ?: estimatedMacros.carbsGram,
                proteinGram = foodDraft.proteinGram ?: estimatedMacros.proteinGram,
                fatGram = foodDraft.fatGram ?: estimatedMacros.fatGram,
                aiFoodName = foodDraft.foodName.trim(),
                aiCalories = reviewedCalories,
                confidence = estimateConfidence(foodDraft)
            )
        }

        return MealRecord(
            id = draft.id,
            ownerId = draft.ownerId,
            memo = draft.memo,
            imageUri = draft.imageUri,
            aiSummary = buildAiSummary(draft),
            createdAt = draft.createdAt,
            foods = reviewedFoods
        )
    }

    private fun buildAiSummary(draft: MealUploadDraft): String {
        val imageText = if (draft.imageUri == null) {
            "사진 없이 사용자 입력값 기준으로 검수했습니다."
        } else {
            "사진과 사용자 입력값을 함께 검수했습니다."
        }
        val autoCalorieCount = draft.foods.count { it.calories == null }
        val calorieText = if (autoCalorieCount > 0) {
            " 칼로리 미입력 음식 ${autoCalorieCount}개는 AI가 추정했습니다."
        } else {
            ""
        }
        return "$imageText 음식 ${draft.foods.size}개를 식사 기록으로 정리했습니다.$calorieText"
    }

    private fun estimateConfidence(foodDraft: MealFoodDraft): Float {
        val macroInputCount = listOf(
            foodDraft.calories,
            foodDraft.carbsGram,
            foodDraft.proteinGram,
            foodDraft.fatGram
        ).count { it != null }
        return when (macroInputCount) {
            4 -> 0.92f
            0 -> 0.68f
            else -> 0.82f
        }
    }
}
