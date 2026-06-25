package com.jaewonlee.aidietrecord.data.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class GoalPlanInput(
    val currentWeightKg: Double,
    val currentMuscleMassKg: Double?,
    val currentBodyFatPercent: Double?,
    val basalMetabolicRateKcal: Int?,
    val targetWeightKg: Double,
    val targetMuscleMassKg: Double?,
    val targetBodyFatPercent: Double?,
    val durationWeeks: Int
)

data class GoalPlanAiResult(
    val calories: Int,
    val carbsGram: Int,
    val proteinGram: Int,
    val fatGram: Int,
    val fiberGram: Int,
    val sugarGram: Int,
    val sodiumMilligram: Int,
    val summary: String
)

class GeminiGoalPlanner {
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")
    }

    suspend fun plan(input: GoalPlanInput): GoalPlanAiResult? = withContext(Dispatchers.IO) {
        val response = model.generateContent(buildPrompt(input))
        response.text?.let(::parseResult)
    }

    private fun buildPrompt(input: GoalPlanInput): String {
        return """
            You are a nutrition coach for a Korean Android diet record app.
            Build a single daily nutrition target that moves the user from their current
            body composition toward their goal over the given period, at a safe pace.

            Guidelines:
            - Weight loss: moderate calorie deficit, higher protein to protect lean mass.
            - Muscle gain: slight calorie surplus, high protein.
            - Maintenance: balance macros around maintenance calories.
            - Keep daily calories within a safe range (about 20-38 kcal per kg of body weight).
            - Protein roughly 1.6-2.2 g per kg of body weight.
            - Use the basal metabolic rate when provided to anchor maintenance calories.

            Return JSON only. No markdown, no code fences, no extra explanation.
            All numeric values must be integers (grams, milligrams, kcal).

            Required JSON shape:
            {
              "calories": 0,
              "carbsGram": 0,
              "proteinGram": 0,
              "fatGram": 0,
              "fiberGram": 0,
              "sugarGram": 0,
              "sodiumMilligram": 0,
              "summary": "short English summary of the plan and its rationale"
            }

            User profile:
            ${buildProfileLines(input)}
        """.trimIndent()
    }

    private fun buildProfileLines(input: GoalPlanInput): String {
        return buildList {
            add("currentWeightKg=${input.currentWeightKg}")
            add("currentMuscleMassKg=${input.currentMuscleMassKg ?: "missing"}")
            add("currentBodyFatPercent=${input.currentBodyFatPercent ?: "missing"}")
            add("basalMetabolicRateKcal=${input.basalMetabolicRateKcal ?: "missing"}")
            add("targetWeightKg=${input.targetWeightKg}")
            add("targetMuscleMassKg=${input.targetMuscleMassKg ?: "missing"}")
            add("targetBodyFatPercent=${input.targetBodyFatPercent ?: "missing"}")
            add("durationWeeks=${input.durationWeeks}")
        }.joinToString("\n")
    }

    private fun parseResult(rawText: String): GoalPlanAiResult? {
        val jsonText = rawText.substringAfter("{", missingDelimiterValue = "")
            .substringBeforeLast("}", missingDelimiterValue = "")
            .takeIf { it.isNotBlank() }
            ?.let { "{$it}" }
            ?: return null

        val root = JSONObject(jsonText)
        val calories = root.optInt("calories", -1).takeIf { it > 0 } ?: return null

        return GoalPlanAiResult(
            calories = calories,
            carbsGram = root.optNonNegativeInt("carbsGram"),
            proteinGram = root.optNonNegativeInt("proteinGram"),
            fatGram = root.optNonNegativeInt("fatGram"),
            fiberGram = root.optNonNegativeInt("fiberGram"),
            sugarGram = root.optNonNegativeInt("sugarGram"),
            sodiumMilligram = root.optNonNegativeInt("sodiumMilligram"),
            summary = root.optString("summary").ifBlank {
                "Gemini generated a daily nutrition plan for your goal."
            }
        )
    }
}

private fun JSONObject.optNonNegativeInt(name: String): Int {
    return optInt(name, 0).coerceAtLeast(0)
}
