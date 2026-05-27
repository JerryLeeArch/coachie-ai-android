package com.jaewonlee.aidietrecord.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.GenerativeBackend
import com.jaewonlee.aidietrecord.data.model.MealFoodDraft
import com.jaewonlee.aidietrecord.data.model.MealUploadDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class MealAiAnalysis(
    val summary: String,
    val foods: List<MealAiFoodAnalysis>
)

data class MealAiFoodAnalysis(
    val foodName: String?,
    val calories: Int?,
    val carbsGram: Int?,
    val proteinGram: Int?,
    val fatGram: Int?,
    val fiberGram: Int?,
    val sugarGram: Int?,
    val sodiumMilligram: Int?,
    val confidence: Float?
)

class GeminiMealAnalyzer(
    context: Context
) {
    private val appContext = context.applicationContext
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash")
    }

    suspend fun analyze(draft: MealUploadDraft): MealAiAnalysis? {
        val prompt = buildPrompt(draft)
        val bitmaps = buildList {
            draft.foods.mapNotNullTo(this) { it.imageUri }
            if (isEmpty()) {
                draft.imageUri?.let(::add)
            }
        }
            .distinct()
            .mapNotNull { uri -> loadScaledBitmap(Uri.parse(uri)) }

        val response = if (bitmaps.isEmpty()) {
            model.generateContent(prompt)
        } else {
            model.generateContent(
                content {
                    bitmaps.forEach { bitmap -> image(bitmap) }
                    text(prompt)
                }
            )
        }

        return response.text?.let(::parseAnalysis)
    }

    private fun buildPrompt(draft: MealUploadDraft): String {
        val foodLines = draft.foods.mapIndexed { index, food ->
            "${index + 1}. ${food.toPromptLine()}"
        }.joinToString("\n")

        return """
            You are a meal nutrition analyzer for a Korean Android diet record app.
            Analyze the provided food photo(s), food names, and free-form descriptions.
            Split one meal into distinct food items when the photo or notes include multiple dishes, sides, drinks, or rice/noodles.
            If the same food appears in both the photo and text notes, include it once and merge the evidence.
            Do not duplicate a menu item just because it was mentioned by both image and text.
            The user no longer enters separate nutrition fields, so infer calories and nutrients from the photos and notes.
            Return JSON only. No markdown, no code fences, no extra explanation.
            Use integer grams/mg/kcal and confidence from 0.0 to 1.0.
            Return one foods array object per distinct food item.

            Required JSON shape:
            {
              "summary": "short English summary",
              "foods": [
                {
                  "foodName": "clear English food name",
                  "calories": 0,
                  "carbsGram": 0,
                  "proteinGram": 0,
                  "fatGram": 0,
                  "fiberGram": 0,
                  "sugarGram": 0,
                  "sodiumMilligram": 0,
                  "confidence": 0.0
                }
              ]
            }

            User memo: ${draft.memo.ifBlank { "(none)" }}
            User foods:
            $foodLines
        """.trimIndent()
    }

    private fun MealFoodDraft.toPromptLine(): String {
        return buildList {
            add("foodName=${foodName.ifBlank { "(unknown)" }}")
            add("description=${description.ifBlank { "(none)" }}")
            add("photoAttached=${imageUri != null}")
            add("calories=${calories ?: "missing"}")
            add("carbsGram=${carbsGram ?: "missing"}")
            add("proteinGram=${proteinGram ?: "missing"}")
            add("fatGram=${fatGram ?: "missing"}")
            add("fiberGram=${fiberGram ?: "missing"}")
            add("sugarGram=${sugarGram ?: "missing"}")
            add("sodiumMilligram=${sodiumMilligram ?: "missing"}")
        }.joinToString(", ")
    }

    private suspend fun loadScaledBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        val resolver = appContext.contentResolver
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(
                width = boundsOptions.outWidth,
                height = boundsOptions.outHeight
            )
        }

        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    }

    private fun calculateSampleSize(
        width: Int,
        height: Int,
        maxDimension: Int = 1024
    ): Int {
        var sampleSize = 1
        var scaledWidth = width
        var scaledHeight = height

        while (scaledWidth > maxDimension || scaledHeight > maxDimension) {
            sampleSize *= 2
            scaledWidth /= 2
            scaledHeight /= 2
        }

        return sampleSize.coerceAtLeast(1)
    }

    private fun parseAnalysis(rawText: String): MealAiAnalysis? {
        val jsonText = rawText.substringAfter("{", missingDelimiterValue = "")
            .substringBeforeLast("}", missingDelimiterValue = "")
            .takeIf { it.isNotBlank() }
            ?.let { "{$it}" }
            ?: return null

        val root = JSONObject(jsonText)
        val foodsJson = root.optJSONArray("foods") ?: return null
        val foods = (0 until foodsJson.length()).mapNotNull { index ->
            val food = foodsJson.optJSONObject(index) ?: return@mapNotNull null
            MealAiFoodAnalysis(
                foodName = food.optString("foodName").ifBlank { null },
                calories = food.optPositiveInt("calories"),
                carbsGram = food.optNonNegativeInt("carbsGram"),
                proteinGram = food.optNonNegativeInt("proteinGram"),
                fatGram = food.optNonNegativeInt("fatGram"),
                fiberGram = food.optNonNegativeInt("fiberGram"),
                sugarGram = food.optNonNegativeInt("sugarGram"),
                sodiumMilligram = food.optNonNegativeInt("sodiumMilligram"),
                confidence = food.optDouble("confidence", Double.NaN)
                    .takeUnless(Double::isNaN)
                    ?.toFloat()
                    ?.coerceIn(0f, 1f)
            )
        }

        return MealAiAnalysis(
            summary = root.optString("summary").ifBlank {
                "Gemini analyzed the meal photo and notes."
            },
            foods = foods
        )
    }
}

private fun JSONObject.optPositiveInt(name: String): Int? {
    return optInt(name, -1).takeIf { it > 0 }
}

private fun JSONObject.optNonNegativeInt(name: String): Int? {
    return optInt(name, -1).takeIf { it >= 0 }
}
